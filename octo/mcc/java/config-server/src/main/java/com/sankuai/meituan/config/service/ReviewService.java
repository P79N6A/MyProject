package com.sankuai.meituan.config.service;

import com.sankuai.meituan.config.domain.*;
import com.sankuai.meituan.config.domain.PullRequest;
import com.sankuai.meituan.config.domain.Review;
import com.sankuai.meituan.config.mapper.PullRequestDetailMapper;
import com.sankuai.meituan.config.mapper.PullRequestMapper;
import com.sankuai.meituan.config.mapper.ReviewMapper;
import com.sankuai.meituan.config.model.Env;
import com.sankuai.meituan.config.model.PropertyValue;
import com.sankuai.octo.config.model.*;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by lhmily on 06/12/2016.
 */
@Component
public class ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewService.class);

    @Resource
    private PullRequestMapper prMapper;
    @Resource
    private PullRequestDetailMapper prDetailMapper;
    @Resource
    private ReviewMapper reviewMapper;
    @Resource
    private ConfigNodeService configNodeService;

    @Transactional
    private boolean handleCreatePR(com.sankuai.octo.config.model.PullRequest pullRequest, List<PRDetail> list) {
        PullRequest prItem = prRequet2PullRequest(pullRequest);
        int count = 0;
        prMapper.insertSelective(prItem);
        for (PRDetail entry : list) {
            PullRequestDetail detail = prEntry2PullRequestDetail(entry);
            detail.setPrId(prItem.getPrId());
            prDetailMapper.insertSelective(detail);
            count++;
        }
        return count == list.size();
    }

    private PullRequestDetail prEntry2PullRequestDetail(PRDetail entry) {
        PullRequestDetail detail = new PullRequestDetail();
        detail.setModifiedKey(entry.key);
        detail.setIsDeleted(entry.isDeleted);
        detail.setNewValue(entry.newValue);
        detail.setNewComment(entry.newComment);
        return detail;
    }

    private PullRequest prRequet2PullRequest(com.sankuai.octo.config.model.PullRequest pullRequest) {
        PullRequest prItem = new PullRequest();
        prItem.setAppkey(pullRequest.appkey);
        prItem.setEnv(pullRequest.env);
        prItem.setNote(pullRequest.note);
        prItem.setPrMisid(pullRequest.prMisID);
        prItem.setPrTime(new Date());
        return prItem;
    }

    public boolean createPR(com.sankuai.octo.config.model.PullRequest pr, List<PRDetail> list) {
        boolean ret = false;
        try {
            ret = handleCreatePR(pr, list);
        } catch (Exception e) {
            LOG.error("Failed to create mcc PR.", e);
            ret = false;
        }
        return ret;
    }

    @Transactional
    private boolean handleDeletePR(Integer prID) {
        PullRequestDetailExample detailExample = new PullRequestDetailExample();
        detailExample.createCriteria().andPrIdEqualTo(prID);
        prDetailMapper.deleteByExample(detailExample);
        prMapper.deleteByPrimaryKey(prID);
        return true;
    }

    public boolean deletePR(long prID) {
        boolean ret = false;
        try {
            Integer prIDInteger = Integer.valueOf((int) prID);
            ret = handleDeletePR(prIDInteger);
        } catch (Exception e) {
            LOG.error("Failed to delete mcc PR, the ID of pr is " + prID, e);
            ret = false;
        }
        return ret;
    }

    @Transactional
    private boolean handleUpdatePRDetail(long prID, List<PRDetail> list) {
        Integer prIDInteger = Integer.valueOf((int) prID);
        PullRequestDetailExample detailExample = new PullRequestDetailExample();
        detailExample.createCriteria().andPrIdEqualTo(prIDInteger);
        List<PullRequestDetail> dbDetailList = prDetailMapper.selectByExample(detailExample);
        List<PullRequestDetail> deleteDetailList = new ArrayList<PullRequestDetail>();
        boolean isExist = false;
        for (PullRequestDetail item : dbDetailList) {
            isExist = false;
            for (PRDetail newItem : list) {
                if (newItem.prDetailID == (long) item.getPrDetailId()) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                deleteDetailList.add(item);
            }
        }

        for (PRDetail newItem : list) {
            for (PullRequestDetail item : dbDetailList) {
                if (newItem.prDetailID == (long) item.getPrDetailId()) {
                    item.setNewValue(newItem.newValue);
                    item.setNewComment(newItem.newComment);
                    item.setIsDeleted(newItem.isDeleted);
                    prDetailMapper.updateByPrimaryKey(item);
                }
            }
        }
        for (PullRequestDetail item : deleteDetailList) {
            prDetailMapper.deleteByPrimaryKey(item.getPrDetailId());
        }
        return true;
    }

    public boolean updatePR(com.sankuai.octo.config.model.PullRequest pullRequest) {
        try {
            Integer prIDInteger = Integer.valueOf((int) pullRequest.prID);

            PullRequest pr = prMapper.selectByPrimaryKey(prIDInteger);
            pr.setNote(pullRequest.note);
            pr.setStatus(pullRequest.status);
            prMapper.updateByPrimaryKey(pr);
        } catch (Exception e) {
            LOG.error("Failed to update mcc PR, the ID of pr is " + pullRequest.prID, e);
            return false;
        }
        return true;
    }

    public boolean updatePRDetail(long prID, List<PRDetail> list) {
        boolean ret = false;
        try {
            ret = handleUpdatePRDetail(prID, list);
        } catch (Exception e) {
            LOG.error("Failed to update mcc PR detail, the ID of pr is " + prID, e);
            ret = false;
        }
        return ret;
    }

    private List<com.sankuai.octo.config.model.PullRequest> dbPR2IdlPr(List<PullRequest> pr) {
        List<com.sankuai.octo.config.model.PullRequest> ret = new ArrayList<com.sankuai.octo.config.model.PullRequest>();
        for (PullRequest item : pr) {
            com.sankuai.octo.config.model.PullRequest newPR = new com.sankuai.octo.config.model.PullRequest();
            newPR.setAppkey(item.getAppkey())
                    .setNote(item.getNote())
                    .setEnv(item.getEnv())
                    .setPrMisID(item.getPrMisid())
                    .setPrTime(item.getPrTime().getTime())
                    .setPrID(item.getPrId().longValue())
                    .setStatus(item.getStatus());
            ret.add(newPR);
        }
        return ret;
    }

    public List<com.sankuai.octo.config.model.PullRequest> getPullRequest(String appkey, int env, int status) {
        List<com.sankuai.octo.config.model.PullRequest> ret = new ArrayList<com.sankuai.octo.config.model.PullRequest>();
        try {
            PullRequestExample example = new PullRequestExample();
            example.createCriteria().andAppkeyEqualTo(appkey).andEnvEqualTo(env).andStatusEqualTo(status);
            List<PullRequest> pr = prMapper.selectByExample(example);
            ret.addAll(dbPR2IdlPr(pr));
        } catch (Exception e) {
            LOG.error("Failed to get PR list.", e);
        }
        return ret;
    }

    private List<PRDetail> dbPrDetail2IdlPrDetail(List<PullRequestDetail> details) {
        List<PRDetail> ret = new ArrayList<PRDetail>();
        for (PullRequestDetail item : details) {
            PRDetail newDetail = new PRDetail();
            newDetail.setPrID(item.getPrId())
                    .setPrDetailID(item.getPrDetailId())
                    .setKey(item.getModifiedKey())
                    .setOldValue(item.getOldValue())
                    .setNewValue(item.getNewValue())
                    .setOldComment(item.getOldComment())
                    .setNewComment(item.getNewComment())
                    .setIsDeleted(item.getIsDeleted());
            ret.add(newDetail);
        }
        return ret;
    }

    public List<PRDetail> getPRDetail(long prID) {
        List<PRDetail> ret = new ArrayList<PRDetail>();
        try {
            PullRequestDetailExample example = new PullRequestDetailExample();
            example.createCriteria()
                    .andPrIdEqualTo((int) prID);
            List<PullRequestDetail> list = prDetailMapper.selectByExample(example);
            ret.addAll(dbPrDetail2IdlPrDetail(list));
        } catch (Exception e) {
            LOG.error("Failed to get PR detail list.", e);
        }
        return ret;
    }

    private Review idlReview2dbReview(com.sankuai.octo.config.model.Review review) {
        Review ret = new Review();
        ret.setNote(review.note);
        ret.setApprove(review.approve);
        ret.setPrId((int) review.prID);
        ret.setReviewerMisid(review.reviewerMisID);
        ret.setReviewTime(new Date());
        return ret;
    }

    private List<com.sankuai.octo.config.model.Review> dbReview2IdlReview(List<Review> list) {
        List<com.sankuai.octo.config.model.Review> ret = new ArrayList<com.sankuai.octo.config.model.Review>();
        for (Review review : list) {
            com.sankuai.octo.config.model.Review item = new com.sankuai.octo.config.model.Review();
            item.setNote(review.getNote());
            item.setApprove(review.getApprove());
            item.setPrID(review.getPrId());
            item.setReviewerMisID(review.getReviewerMisid());
            item.setReviewTime(review.getReviewTime().getTime());
            item.setReviewID(review.getReviewId());
            ret.add(item);
        }
        return ret;
    }

    @Transactional
    private boolean handleCreateReview(com.sankuai.octo.config.model.Review review){
        Review dbReview = idlReview2dbReview(review);
        reviewMapper.insertSelective(dbReview);
        if(review.approve == -1){
            PullRequest pr=prMapper.selectByPrimaryKey(dbReview.getPrId());
            pr.setStatus(-1);
            prMapper.updateByPrimaryKey(pr);
        }
        return true;
    }

    public boolean createReview(com.sankuai.octo.config.model.Review review) {
        boolean ret = false;
        try {
            ret = handleCreateReview(review);
        } catch (Exception e) {
            LOG.error("Failed to create PR review.", e);
            ret = false;
        }
        return ret;
    }

    public List<com.sankuai.octo.config.model.Review> getReview(long prID) {
        List<com.sankuai.octo.config.model.Review> ret = new ArrayList<com.sankuai.octo.config.model.Review>();
        try {
            ReviewExample example = new ReviewExample();
            example.createCriteria()
                    .andPrIdEqualTo((int) prID);
            example.setOrderByClause("review_time desc");
            List<Review> list = reviewMapper.selectByExample(example);
            ret.addAll(dbReview2IdlReview(list));
        } catch (Exception e) {
            LOG.error("Failed to get PR detail list.", e);
        }
        return ret;
    }

    @Transactional
    private boolean handleMergePR(int prID) {
        ReviewExample reviewExample = new ReviewExample();
        reviewExample.createCriteria()
                .andPrIdEqualTo(prID)
                .andApproveEqualTo(1);
        if (reviewMapper.countByExample(reviewExample) <= 0) {
            return false;
        }

        PullRequest pr = prMapper.selectByPrimaryKey(prID);

        String spacePath = pr.getAppkey() + "/" + Env.get(pr.getEnv()).name();

        for(int count = 0; count < 3; ++count){
            Stat status = new Stat();
            Map<String, PropertyValue> config = configNodeService.getDataMap(spacePath, status);

            PullRequestDetailExample detailExample = new PullRequestDetailExample();
            detailExample.createCriteria().andPrIdEqualTo(prID);

            List<PullRequestDetail> detailList = prDetailMapper.selectByExample(detailExample);
            for (PullRequestDetail item : detailList) {
                if (item.getIsDeleted()) {
                    config.remove(item.getModifiedKey());
                } else {
                    PropertyValue value = config.get(item.getModifiedKey());
                    if (null != value) {
                        value.setValue(item.getNewValue());
                        value.setComment(item.getNewComment());
                    } else {
                        value = new PropertyValue();
                        value.setKey(item.getModifiedKey());
                        value.setValue(item.getNewValue());
                        value.setComment(item.getNewComment());
                        config.put(item.getModifiedKey(), value);
                    }
                }
            }
            try{
                configNodeService.reset(spacePath, config.values(), status.getVersion());
            }catch (KeeperException.BadVersionException e){
                // retry again.
                continue;
            }catch (Exception e){
                LOG.error("fail to update.", e);
                return false;
            }
            pr.setStatus(1);
            prMapper.updateByPrimaryKey(pr);
            return true;
        }
        return true;

    }

    public boolean mergePR(long prID) {
        boolean ret = false;
        try {
            ret = handleMergePR((int) prID);
        } catch (Exception e) {
            LOG.error("Failed to Merge PR.", e);
            ret = false;
        }
        return ret;
    }
}
