# spring boot use minio as Object Storage

## config minio information in application.yml
```yaml
# minio 文件存储配置信息
minio:
  endpoint: http://your.minio.com:9000
  accessKey: username
  secretKey: password
  bucketName: sourceMaterial
```

## 测试接口地址信息 URL： 
* http://127.0.0.1:8080/file.html             # 上传
* http://127.0.0.1:8080/list?start=pro        # 获取以pro开头的文件接口
* http://127.0.0.1:8080/buckets               # 获取所有buckets 接口
* http://127.0.0.1:8080/download?file=product_1609405316748.jpg   # 下载接口
* http://127.0.0.1:8080/remove?file=product_1609482784902.png       # 删除文件接口
* http://127.0.0.1:8080/getUrl?file=product_1609405316748.jpg  # 获取原生的下载路径及删除地址信息等？

## 设置图片为公共可访问；
* http://your.minio.com:9000/public/product_1609405316748.jpg

## Docker下运行Minio
```shell script
docker run -p 9000:9000 \
  -e "MINIO_ROOT_USER=AKIAIOSFODNN7EXAMPLE" \
  -e "MINIO_ROOT_PASSWORD=wJalrXUtnFEMIK7MDENGbPxRfiCYEXAMPLEKEY" \
  minio/minio:RELEASE.2020-01-25T02-50-51Z server /data

```