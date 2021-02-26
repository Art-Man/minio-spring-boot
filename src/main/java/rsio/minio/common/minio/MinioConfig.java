package rsio.minio.common.minio;


import io.minio.MinioClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * minio 核心配置类
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MinioProp.class)
public class MinioConfig {

    @Autowired
    private MinioProp minioProp;

    /**
     * 获取 MinioClient
     *
     * @return
     */
    @Bean
    @SneakyThrows
    public MinioClient minioClient() {
        return MinioClient.builder()
                        .endpoint(minioProp.getEndpoint())
                        .credentials(minioProp.getAccessKey(), minioProp.getSecretKey())
                        .build();
    }
}