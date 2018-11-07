package com.sankuai.octo.msgp.model.reqpolicy;

/**
 * Created by Jsong on 2018/7/29.
 */
public enum ResponseMessage {

    EC_APPKEY_EMPTY(400001, "appkey参数错误，不允许为空"),
    EC_ENV_ONLINE(400002, "env参数错误,仅仅支持线上环境"),
    EC_ENV_OFFLINE(400003, "env参数错误,仅仅支持线下环境"),
    EC_ImageUrlV2_EMPTY(400004, "imageUrlV2参数错误，不允许为空"),
    EC_SRV_MORE_THAN_ONE(400010, "单个appkey绑定多个服务单元"),
    EC_SRV_NOT_EXIST(400011, "appkey查询不到对应的服务单元"),
    EC_RELEASE_MORE_THAN_ONE(400012, "单个appkey应用多个发布项"),
    EC_RELEASET_NOT_EXIST(400013, "appkey查询不到应用发布项"),
    EC_PLUS_TEMPLATES_UNFIT(400020, "应用发布项存在多个模板"),
    EC_PARSE_SRV_FROM_OPS_FAILED(400100, "无法从ops解析出srv信息"),
    EC_PARSE_RELEASE_FROM_OPS_FAILED(400101, "无法从ops解析出发布项信息"),
    EC_APPKEY_NOT_EXIST_IN_OPS(404001, "从srv获取ops失败，appkey可能不在ops上"),
    EC_RELEASE_NOT_EXIST_IN_PLUS(404002, "查询不到指定appkey的发布项信息, srvs可能不再plus上"),
    EC_STABLE_IMAGES_NO_EXIST(404003, "无稳定镜像记录"),
    EC_GET_SRV__FROM_OPS_FAILED(500001, "无法从ops获取srv信息。原因：ops服务有问题"),
    EC_GET_RELEASE_FROM_OPS_FAILED(500002, "无法从ops获取应用发布项信息，原因：ops服务有问题"),
    EC_GET_STABLE_IMAGES_FAILED(500003, "无单容器的稳定镜像"),
    EC_GET_PLUS_TEMPLATE_FAILED(500004, "无法获取发布项信息，原因：plus服务有问题"),
    EC_SRV_EMPTY(500100, "ops上srv信息为空"),
    EC_RELEASE_EMPTY(500101, "ops上发布项信息为空"),
    EC_GET_TEMPLATE_FAILED(500007, "无法从plus获取发布包列表，原因：plus系统有问题"),
    EC_SUCCESS(200, "success");

    private final Integer status;

    private final String message;

    ResponseMessage(final Integer status, final String message) {
        this.status = status;
        this.message = message;
    }

    public static Integer getValue(Integer value) {
        ResponseMessage[] businessModeEnums = values();
        for (ResponseMessage businessModeEnum : businessModeEnums) {
            if (businessModeEnum.status().equals(value)) {
                return businessModeEnum.status();
            }
        }
        return null;
    }

    public static String getMessage(Integer value) {
        ResponseMessage[] businessModeEnums = values();
        for (ResponseMessage businessModeEnum : businessModeEnums) {
            if (businessModeEnum.status().equals(value)) {
                return businessModeEnum.message();
            }
        }
        return null;
    }

    public Integer status() {
        return this.status;
    }

    public String message() {
        return this.message;
    }
}
