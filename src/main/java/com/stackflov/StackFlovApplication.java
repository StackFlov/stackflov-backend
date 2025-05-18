package com.stackflov;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@SpringBootApplication
public class StackFlovApplication {
	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		System.out.println("현재 JVM 기본 TimeZone: " + TimeZone.getDefault().getID());
		System.out.println("서버 현재 시간: " + ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
	}
	public static void main(String[] args) {
		SpringApplication.run(StackFlovApplication.class, args);
	}

}
