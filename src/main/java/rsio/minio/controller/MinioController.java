package rsio.minio.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rsio.minio.common.minio.MinioUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

@Controller
@RestController
@Slf4j
public class MinioController {

    @Autowired
    private MinioUtils minioUtils;

    /**
     * 文件上传接口
     *
     * @param file
     * @param request
     * @return
     */
    @PostMapping("/upload")
    @ResponseBody
    public String upload(@RequestParam(name = "file", required = false) MultipartFile file, HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            res = minioUtils.uploadFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            res.put("code", 0);
            res.put("msg", "上传失败");
        }
        return res.toJSONString();
    }

    /**
     * 列出所有以开头的文件
     * @param start
     * @return
     */
    @GetMapping("/list")
    @ResponseBody
    public String list(String start) {
        JSONObject res = new JSONObject();
        try {
            res = minioUtils.listFiles(start);
        } catch (Exception e) {
            e.printStackTrace();
            res.put("code", 0);
            res.put("msg", "获取失败");
        }
        return res.toJSONString();
    }

    /**
     * 移除文件
     * @param file
     * @return
     */
    @GetMapping("/remove")
    @ResponseBody
    public String remove(String file) {
        JSONObject res = new JSONObject();
        try {
            res = minioUtils.remove(file);
        } catch (Exception e) {
            e.printStackTrace();
            res.put("code", 0);
            res.put("msg", "移除失败");
        }
        return res.toJSONString();
    }

    /**
     * 获取URL路径
     * @param file
     * @return
     */
    @GetMapping("/getUrl")
    @ResponseBody
    public String getPresignedObject(String file) {
        JSONObject res = new JSONObject();
        try {
            res = minioUtils.getPresignedObject(file);
        } catch (Exception e) {
            e.printStackTrace();
            res.put("code", 0);
            res.put("msg", "获取文件URL失败");
        }
        return res.toJSONString();
    }

    /**
     * 下载文件
     * @param file
     * @return
     */
    @GetMapping(path = "/download")
    public ResponseEntity<ByteArrayResource> uploadFile(@RequestParam(value = "file") String file) {
        byte[] data = minioUtils.getFile(file);
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + file + "\"")
                .body(resource);
    }

    @GetMapping(path = "/buckets")
    public JSONObject listBuckets() {
        JSONObject res = new JSONObject();
        try {
            res = minioUtils.getAllBuckets();
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            res.put("code", 0);
            res.put("msg", "获取失败:"+e.getMessage());
        }
        return res;
    }





    }