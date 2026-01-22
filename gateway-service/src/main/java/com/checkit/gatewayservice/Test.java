package com.checkit.gatewayservice;

import com.checkit.common.dto.ApiResponse;
import com.checkit.common.exception.CommonCode;
import java.io.*;
import java.util.*;

public class Test {
    public void testMethod() {
        ApiResponse<String> response = ApiResponse.success("test");
        System.out.println(CommonCode.SUCCESS.getCode());
    }
}
