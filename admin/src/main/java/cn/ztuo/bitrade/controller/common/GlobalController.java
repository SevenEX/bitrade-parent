package cn.ztuo.bitrade.controller.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Controller
@Slf4j
public class GlobalController {

    @ExceptionHandler({Exception.class})
    public String exception(Exception e) {
        log.info(e.getMessage());
        return "exception";
    }
}

