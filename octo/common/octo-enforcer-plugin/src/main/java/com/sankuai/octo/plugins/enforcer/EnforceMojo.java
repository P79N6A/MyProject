package com.sankuai.octo.plugins.enforcer;

import com.alibaba.fastjson.JSON;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;

@Mojo(name = "enforce", defaultPhase = LifecyclePhase.VALIDATE,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        threadSafe = true)
public class EnforceMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    private static final String OCTO_API = "http://octo.sankuai.com/api";
    private boolean onlineBuild = false;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.project == null) {
            getLog().warn("Can not find a Maven project!!!");
        }
        try {
            onlineBuild = isOnlineBuild();
            getLog().info("onlineBuild : " + onlineBuild);
            if (onlineBuild) {
                uploadArtifacts();
            }
            String config = getConfigFromOcto();
            List<SimpleArtifact> checkArtifacts = parseConfig(config);
            getLog().debug("checkArtifacts : " + checkArtifacts);
            Map<SimpleArtifact, Artifact> brokenArtifacts = getBrokenArtifacts(checkArtifacts);
            if (!brokenArtifacts.isEmpty()) {
                doBrokenAction(brokenArtifacts);
            }
        } catch (MojoExecutionException ex) {
            throw ex;
        } catch (MojoFailureException ex) {
            throw ex;
        } catch (Exception ex) {
            getLog().warn(ex.getMessage(), ex);
        }
    }

    private boolean isOnlineBuild() {
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(2000).build();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        try {
            String ip = Util.getLocalIpV4();
            String hostname = Util.getHostNameInfoByIp();
            String params = "ip=" + ip + "&hostname=" + hostname;
            getLog().debug(params);
            HttpGet httpGet = new HttpGet(OCTO_API + "/plugin/online?" + params);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            InputStream is = response.getEntity().getContent();
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("utf-8")));
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            String result = sb.toString();
            response.close();
            br.close();
            httpClient.close();
            return "true".equalsIgnoreCase(result);
        } catch (Exception e) {
            getLog().info("isOnlineBuild failed... ", e);
            return false;
        }
    }

    private void uploadArtifacts() {
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(2000).build();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        HttpPost httpPost = new HttpPost(OCTO_API + "/plugin/upload");
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("project", toSimple(getProject().getArtifact()));
            map.put("dependency", toSimple(getProject().getRuntimeArtifacts()));
            String jsonText = JSON.toJSONString(map);
            getLog().debug(jsonText);
            httpPost.setEntity(new StringEntity(jsonText));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            InputStream is = response.getEntity().getContent();
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("utf-8")));
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            String result = sb.toString();
            getLog().debug(result);
            response.close();
            br.close();
            httpClient.close();
        } catch (Exception e) {
            getLog().info("uploadArtifacts failed... ", e);
        }
    }

    private SimpleArtifact toSimple(Artifact artifact) {
        return new SimpleArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
    }

    private List<SimpleArtifact> toSimple(List<Artifact> artifacts) {
        List<SimpleArtifact> simpleArtifacts = new ArrayList<SimpleArtifact>();
        for (Artifact artifact : artifacts) {
            simpleArtifacts.add(toSimple(artifact));
        }
        return simpleArtifacts;
    }

    private String getConfigFromOcto() {
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(2000).build();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        String result = null;
        try {
            Artifact artifact = getProject().getArtifact();
            String params = "groupId=" + artifact.getGroupId() + "&artifactId=" + artifact.getArtifactId();
            getLog().debug(params);
            HttpGet httpGet = new HttpGet(OCTO_API + "/plugin/config?" + params);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            InputStream is = response.getEntity().getContent();
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("utf-8")));
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            result = sb.toString();
            response.close();
            br.close();
            httpClient.close();
        } catch (Exception e) {
            getLog().info("getConfigFromOcto failed... ", e);
        }
        return result;
    }

    private List<SimpleArtifact> parseConfig(String config) {
        List<SimpleArtifact> artifacts = JSON.parseArray(config, SimpleArtifact.class);
        return artifacts;
    }

    private Map<SimpleArtifact, Artifact> getBrokenArtifacts(List<SimpleArtifact> checkArtifacts) throws MojoExecutionException {
        Set<Artifact> artifacts = getProject().getArtifacts();
        Map<SimpleArtifact, Artifact> brokenArtifacts = new HashMap();
        for (Iterator sait = checkArtifacts.iterator(); sait.hasNext(); ) {
            SimpleArtifact simpleArtifact = (SimpleArtifact) sait.next();
            for (Iterator it = artifacts.iterator(); it.hasNext(); ) {
                Artifact artifact = (Artifact) it.next();
                if (simpleArtifact.compareTo(artifact) > 0) {
                    brokenArtifacts.put(simpleArtifact, artifact);
                } else {
                    continue;
                }
            }
        }
        return brokenArtifacts;
    }

    private void doBrokenAction(Map<SimpleArtifact, Artifact> brokenArtifacts) throws MojoFailureException {
        getLog().info("The version of following files are invalid.");
        Set<String> artifacts = new HashSet<String>();
        for (SimpleArtifact sa : brokenArtifacts.keySet()) {
            Artifact artifact = brokenArtifacts.get(sa);
            getLog().error(sa.getGroupId() + ":" + sa.getArtifactId() + " required version[" + sa.getVersion() + "], actual version[" + artifact.getVersion() + "]");
            if (onlineBuild && "broken".equalsIgnoreCase(sa.getAction())) {
                artifacts.add(sa.getArtifactId());
            }
        }
        if (!artifacts.isEmpty()) {
            String message = "please upgrade " + artifacts + " to the specified version";
            throw new MojoFailureException(message);
        }
    }

    public MavenProject getProject() {
        return this.project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }
}