package bestDAOU.PicMessage_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // 생성일자 자동 기록 활성화
public class PicMessageBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PicMessageBackendApplication.class, args);
	}

}
