package rsio.minio.common.minio;

import com.alibaba.fastjson.JSONObject;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MinioUtils {

    @Autowired
    private MinioClient client;
    @Autowired
    private MinioProp minioProp;

    /**
     * 上传文件
     *
     * @param file 文件
     * @return
     */
    @SneakyThrows
    public JSONObject uploadFile(MultipartFile file) {
        JSONObject res = new JSONObject();
        res.put("code", 1);
        // 判断上传文件是否为空
        if (null == file || 0 == file.getSize()) {
            res.put("msg", "上传文件不能为空");
            return res;
        }

        // 判断存储桶是否存在 并 创建bucket
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(minioProp.getBucketName()).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(minioProp.getBucketName()).build());
        }

        // 文件名
        String originalFilename = file.getOriginalFilename();
        // 新的文件名 = 存储桶名称_时间戳.后缀名
        String fileName = minioProp.getBucketName() + "_" + System.currentTimeMillis() + originalFilename.substring(originalFilename.lastIndexOf("."));

        // 开始上传 本地文件，服务器本地的文件
        //client.uploadObject(
        //       UploadObjectArgs.builder()
        //               .bucket(minioProp.getBucketName())
        //               .object(fileName)
        //               .filename("/home/path/to/file.jpg")
        //               .contentType(file.getContentType())
        //               .build());
        // 上传file object 对象文件
        client.putObject(
                PutObjectArgs.builder()
                        .bucket(minioProp.getBucketName())
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
// 以前的 minio java 3.0.10 版本
//        client.putObject(minioProp.getBucketName(), fileName, file.getInputStream(), file.getContentType());
        res.put("code", 0);
        res.put("msg", minioProp.getEndpoint() + "/" + minioProp.getBucketName() + "/" + fileName);
        return res;
    }

    /**
     * 列出文件列表
     *
     * @param start
     * @return
     */
    @SneakyThrows
    public JSONObject listFiles(String start) {
        JSONObject res = new JSONObject();
        res.put("code", 0);

        Iterable<Result<Item>> results = null;

//        // Lists objects information.
//        results = client.listObjects(ListObjectsArgs.builder().bucket(minioProp.getBucketName()).build());
//        res.put("objects1", getResult(results));
//
//        // Lists objects information recursively.
//        results = client.listObjects(ListObjectsArgs.builder().bucket(minioProp.getBucketName()).recursive(true).build());
//        res.put("objects2", getResult(results));

        // Lists maximum 100 objects information those names starts with 'E' and after
        // 'ExampleGuide.pdf'.
        results = client.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minioProp.getBucketName())
//                        .startAfter("product")
                        .prefix(start)
                        .maxKeys(100)
                        .build());
        res.put("objects3", getResult(results));


//        // Lists maximum 100 objects information with version those names starts with 'E' and after
//        // 'ExampleGuide.pdf'.
//        results = client.listObjects(
//                ListObjectsArgs.builder()
//                        .bucket(minioProp.getBucketName())
//                        .startAfter(start)
////                                .startAfter("ExampleGuide.pdf")
////                                .prefix("E")
//                        .maxKeys(100)
//                        .includeVersions(true)
//                        .build());
//        res.put("objects4", getResult(results));

        return res;
    }

    /**
     * 格式化输出
     * @param results
     * @return
     */
    @SneakyThrows
    private List<String> getResult(Iterable<Result<Item>> results) {
        List<String> list = new ArrayList<>();
        results.forEach(result -> {
            Item item = null;
            try {
                item = result.get();
            } catch (Exception e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
//            list.add(minioProp.getEndpoint() + "/" + minioProp.getBucketName() + "/" + item.objectName());
            list.add("/download?file=" + item.objectName());
//            list.add(item.lastModified() + "  " + item.size() + "  " + item.objectName());
        });
        return list;
    }

    /**
     * 下载文件，获取文件字节流
     * @param key
     * @return
     */
    @SneakyThrows
    public byte[] getFile(String key) {
        InputStream obj = client.getObject(GetObjectArgs.builder().bucket(minioProp.getBucketName()).object(key).build());
        byte[] content = IOUtils.toByteArray(obj);
        obj.close();
        return content;
    }

    /**
     * 获取对象URL信息
     * @param key
     * @return
     */
    @SneakyThrows
    public JSONObject getPresignedObject(String key) {
        JSONObject res = new JSONObject();
        res.put("code", 0);
        // Get presigned URL of an object for HTTP method, expiry time and custom request parameters.
        String url = null;
        url = client.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.DELETE)
                        .bucket(minioProp.getBucketName())
                        .object(key)
                        .expiry(24 * 60 * 60)
                        .build());
        res.put("Method.DELETE", url);

        // Get presigned URL string to upload 'my-objectname' in 'my-bucketname'
        // with response-content-type as application/json and life time as one day.
        Map<String, String> reqParams = new HashMap<String, String>();
        reqParams.put("response-content-type", "application/json");

        url = client.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(minioProp.getBucketName())
                        .object(key)
                        .expiry(1, TimeUnit.DAYS)
                        .extraQueryParams(reqParams)
                        .build());
        res.put("Method.PUT", url);

        // Get presigned URL string to download 'my-objectname' in 'my-bucketname' and its life time
        // is 2 hours.
        url = client.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(minioProp.getBucketName())
                        .object(key)
                        .expiry(2, TimeUnit.HOURS)
                        .build());
        System.out.println(url);

        res.put("Method.GET", url);
        return res;
    }

    @SneakyThrows
    public JSONObject getAllBuckets() {
        JSONObject res = new JSONObject();
        res.put("code", 0);
        List<String> list = new ArrayList();
        List<Bucket> buckets = client.listBuckets();
        buckets.forEach(bucket -> {
            list.add(bucket.name());
        });
        res.put("res", list);
        return res;
    }

    /**
     * 移除或删除对象
     *
     * @param file
     * @return
     * @throws Exception
     */
    public JSONObject remove(String file) throws Exception {
        JSONObject res = new JSONObject();
        res.put("code", 0);
        res.put("msg", "删除成功");
        // Remove object.
        client.removeObject(RemoveObjectArgs.builder().bucket(minioProp.getBucketName()).object(file).build());

//        // Remove versioned object.
//        client.removeObject(
//                RemoveObjectArgs.builder()
//                        .bucket(minioProp.getBucketName())
//                        .object(file)
//                        .versionId("my-versionid")
//                        .build());
        return res;
    }
}