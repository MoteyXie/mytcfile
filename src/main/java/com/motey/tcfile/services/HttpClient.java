package com.motey.tcfile.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class HttpClient {

//	public String client(String url, HttpMethod method, MultiValueMap<String, ?> params) {
//		RestTemplate template = new RestTemplate();
//		ResponseEntity<String> response1 = template.getForEntity(url, String.class);
//		return response1.getBody();
//	}

    public String client(String url, HttpMethod method, MultiValueMap<String, ?> params){
        RestTemplate client = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        //请勿轻易改变此提交方式，大部分的情况下，提交方式都是表单提交
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, ?>> requestEntity = new HttpEntity<MultiValueMap<String, ?>>(params, headers);
        //  执行HTTP请求
        ResponseEntity<String> response = client.exchange(url, method, requestEntity, String.class);
        return response.getBody();
    }
}
