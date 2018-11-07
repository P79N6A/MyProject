package com.sankuai.octo.msgp.utils;

/**
 * Created by zava on 16/1/26.
 */

import com.sankuai.msgp.common.utils.HttpUtil;
import com.sankuai.msgp.common.utils.StringUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

public class BodyReaderHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private static final Logger logger = LoggerFactory.getLogger(BodyReaderHttpServletRequestWrapper.class);


    private byte[] body;
    private Map<String, String[]> allParameters = null;
    private String characterEncoding;

    private ByteArrayOutputStream byteArrayOutputStream;

    /**
     * @param request
     */
    public BodyReaderHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        try {
            characterEncoding = request.getCharacterEncoding();
            if (StringUtil.isBlank(characterEncoding)) {
                characterEncoding = "UTF-8";
            }
            if (isFormPost() || isFormPut()) {
                getRequestPayload(request);
            } else {
                getRequestParams(request);
            }

        } catch (Exception e) {
            logger.info("getInputStream error", e);
        }
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bais;
        if (body.length == 0) {
            if (null == byteArrayOutputStream) {
                copyInputStream(super.getInputStream());
            }
            bais = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        } else {
            bais = new ByteArrayInputStream(body);
        }
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return bais.read();
            }
        };
    }

    private void copyInputStream(ServletInputStream input) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = input.read(buffer)) > -1) {
            baos.write(buffer, 0, len);
        }
        baos.flush();
        this.byteArrayOutputStream = baos;
    }

    private void getRequestPayload(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = request.getReader();
            char[] buff = new char[1024];
            int len;
            while ((len = reader.read(buff)) != -1) {
                sb.append(buff, 0, len);
            }

        } catch (Exception e) {
            logger.error("getRequestPayload error", e);
        }
        body = sb.toString().getBytes();
    }

    private void getRequestParams(HttpServletRequest request) {
        try {
            int contentLength = request.getContentLength();
            ByteArrayOutputStream cachedContent = new ByteArrayOutputStream(contentLength >= 0 ? contentLength : 1024);
            Map<String, String[]> form = super.getParameterMap();
            for (Iterator<String> nameIterator = form.keySet().iterator(); nameIterator.hasNext(); ) {
                String name = nameIterator.next();
                List<String> values = Arrays.asList(form.get(name));
                for (Iterator<String> valueIterator = values.iterator(); valueIterator.hasNext(); ) {
                    String value = valueIterator.next();
                    cachedContent.write(URLEncoder.encode(name, characterEncoding).getBytes());
                    if (value != null) {
                        cachedContent.write('=');
                        cachedContent.write(URLEncoder.encode(value, characterEncoding).getBytes());
                        if (valueIterator.hasNext()) {
                            cachedContent.write('&');
                        }
                    }
                }
                if (nameIterator.hasNext()) {
                    cachedContent.write('&');
                }
            }
            allParameters = form;
            body = cachedContent.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write request parameters to cached content", ex);
        }
    }

    @Override
    public String getParameter(final String name) {
        String[] strings = getParameterMap().get(name);
        if (strings != null) {
            return strings[0];
        }
        return null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (allParameters == null) {
            allParameters = new HashMap<String, String[]>();
            try {
                String inputString = IOUtils.toString(getInputStream(), characterEncoding);
                Map<String, Object> map = HttpUtil.getUrlParams(inputString);
                for (String key : map.keySet()) {
                    String[] values = {(String) map.get(key)};
                    allParameters.put(key, values);
                }
            } catch (Exception e) {
                logger.error("get getUrlParams error", e);
            }

        }
        return Collections.unmodifiableMap(allParameters);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(getParameterMap().keySet());
    }

    @Override
    public String[] getParameterValues(final String name) {
        return getParameterMap().get(name);
    }

    private boolean isFormPut() {
        return "PUT".equalsIgnoreCase(getMethod());
    }

    private boolean isFormPost() {
        return "POST".equalsIgnoreCase(getMethod());
    }
}