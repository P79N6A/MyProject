namespace java com.sankuai.octo.idc.model
namespace cpp com.sankuai.cmtrace

/**
 * regions for mtdp
 */

struct Idc {
    1:string region;
    2:string idc;
    3:optional string center; //中心归属 BJ1 BJ2 NOCENTER SH
}
