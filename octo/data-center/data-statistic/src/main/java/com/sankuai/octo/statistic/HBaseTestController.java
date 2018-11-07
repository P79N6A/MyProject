/*
package com.sankuai.octo.statistic;

import com.sankuai.octo.statistic.helper.api;
import com.sankuai.octo.statistic.util.InstanceDump;
import com.sankuai.octo.statistic.util.InstanceDumpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

*/
/**
 * Created by wujinwu on 15/12/4.
 *//*

@Controller
public class HBaseTestController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/instance/dump", method = RequestMethod.GET)
    public void dumpInstance(HttpServletRequest request, HttpServletResponse response) {

        // get dump info
        InstanceDump dump = InstanceDumpHelper.getSnapshot();
        //  write to file
        ServletContext ctx = request.getServletContext();
        long randomFileName = System.currentTimeMillis();
        String path = ctx.getRealPath("/" + randomFileName + ".txt");
        File file = new File(path);
        try {
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(api.jsonStr(dump));
            }

            String mimeType = URLConnection.guessContentTypeFromName(file.getName());
            if (mimeType == null) {
                logger.info("mimetype is not detectable, will take default");
                mimeType = "application/octet-stream";
            }

            logger.info("mimetype : {}", mimeType);

            response.setContentType(mimeType);

            //  "Content-Disposition : attachment" will be directly download, may provide save as popup, based on your browser setting
            response.setHeader("Content-Disposition", "attachment; filename=\"dump.txt\"");

            response.setContentLength((int) file.length());

            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

            //Copy bytes from source to destination(outputstream in this example), closes both streams.
            ServletOutputStream responseOutputStream = response.getOutputStream();
            FileCopyUtils.copy(inputStream, responseOutputStream);

        } catch (IOException ex) {
            logger.info("Error writing file to output stream.", ex);
            throw new RuntimeException("IOError writing file to output stream");
        } finally {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }

    }

}
*/
