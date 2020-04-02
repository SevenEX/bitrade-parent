package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.enums.CredentialsType;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.RedisUtil;
import cn.ztuo.bitrade.util.RedissonUtil;
import lombok.extern.log4j.Log4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

@RestController
@Log4j
public class LocalizationTestController extends BaseController {
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private RedisUtil redisUtil;
	/**
	 * 请求测试用国际化文本
	 */
	@RequestMapping("/getTestMessage")
	public MessageResult getMessage() throws IOException {
		return success(messageSource.getMessage("TEST", null, LocaleContextHolder.getLocale()));
	}
	@RequestMapping("/getEnumTestMessage")
	public MessageResult getEnumMessage() throws IOException {
		return success(Arrays.asList(CredentialsType.CARDED, CredentialsType.PASSPORT, CredentialsType.DRIVING_LICENSE));
	}
	@RequestMapping("redisTest")
	public String redisTest() {
		RBucket<Date> test = RedissonUtil.getBucket("test");
		test.set(new Date());
		log.info(test.get());
		redisUtil.set("bucket:test", new Date());
		Object testData = redisUtil.get("bucket:test");
		log.info(testData);
		test.delete();
		return "OK";
	}

}
