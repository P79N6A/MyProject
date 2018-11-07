package com.sankuai.meituan.config.domain;

import java.util.ArrayList;
import java.util.List;

public class PullRequestDetailExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public PullRequestDetailExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andPrDetailIdIsNull() {
            addCriterion("pr_detail_id is null");
            return (Criteria) this;
        }

        public Criteria andPrDetailIdIsNotNull() {
            addCriterion("pr_detail_id is not null");
            return (Criteria) this;
        }

        public Criteria andPrDetailIdEqualTo(Integer value) {
            addCriterion("pr_detail_id =", value, "prDetailId");
            return (Criteria) this;
        }

        public Criteria andPrDetailIdNotEqualTo(Integer value) {
            addCriterion("pr_detail_id <>", value, "prDetailId");
            return (Criteria) this;
        }

        public Criteria andPrDetailIdGreaterThan(Integer value) {
            addCriterion("pr_detail_id >", value, "prDetailId");
            return (Criteria) this;
        }

        public Criteria andPrDetailIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("pr_detail_id >=", value, "prDetailId");
            return (Criteria) this;
        }

        public Criteria andPrDetailIdLessThan(Integer value) {
            addCriterion("pr_detail_id <", value, "prDetailId");
            return (Criteria) this;
        }

        public Criteria andPrDetailIdLessThanOrEqualTo(Integer value) {
            addCriterion("pr_detail_id <=", value, "prDetailId");
            return (Criteria) this;
        }

        public Criteria andPrDetailIdIn(List<Integer> values) {
            addCriterion("pr_detail_id in", values, "prDetailId");
            return (Criteria) this;
        }

        public Criteria andPrDetailIdNotIn(List<Integer> values) {
            addCriterion("pr_detail_id not in", values, "prDetailId");
            return (Criteria) this;
        }

        public Criteria andPrDetailIdBetween(Integer value1, Integer value2) {
            addCriterion("pr_detail_id between", value1, value2, "prDetailId");
            return (Criteria) this;
        }

        public Criteria andPrDetailIdNotBetween(Integer value1, Integer value2) {
            addCriterion("pr_detail_id not between", value1, value2, "prDetailId");
            return (Criteria) this;
        }

        public Criteria andPrIdIsNull() {
            addCriterion("pr_id is null");
            return (Criteria) this;
        }

        public Criteria andPrIdIsNotNull() {
            addCriterion("pr_id is not null");
            return (Criteria) this;
        }

        public Criteria andPrIdEqualTo(Integer value) {
            addCriterion("pr_id =", value, "prId");
            return (Criteria) this;
        }

        public Criteria andPrIdNotEqualTo(Integer value) {
            addCriterion("pr_id <>", value, "prId");
            return (Criteria) this;
        }

        public Criteria andPrIdGreaterThan(Integer value) {
            addCriterion("pr_id >", value, "prId");
            return (Criteria) this;
        }

        public Criteria andPrIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("pr_id >=", value, "prId");
            return (Criteria) this;
        }

        public Criteria andPrIdLessThan(Integer value) {
            addCriterion("pr_id <", value, "prId");
            return (Criteria) this;
        }

        public Criteria andPrIdLessThanOrEqualTo(Integer value) {
            addCriterion("pr_id <=", value, "prId");
            return (Criteria) this;
        }

        public Criteria andPrIdIn(List<Integer> values) {
            addCriterion("pr_id in", values, "prId");
            return (Criteria) this;
        }

        public Criteria andPrIdNotIn(List<Integer> values) {
            addCriterion("pr_id not in", values, "prId");
            return (Criteria) this;
        }

        public Criteria andPrIdBetween(Integer value1, Integer value2) {
            addCriterion("pr_id between", value1, value2, "prId");
            return (Criteria) this;
        }

        public Criteria andPrIdNotBetween(Integer value1, Integer value2) {
            addCriterion("pr_id not between", value1, value2, "prId");
            return (Criteria) this;
        }

        public Criteria andModifiedKeyIsNull() {
            addCriterion("modified_key is null");
            return (Criteria) this;
        }

        public Criteria andModifiedKeyIsNotNull() {
            addCriterion("modified_key is not null");
            return (Criteria) this;
        }

        public Criteria andModifiedKeyEqualTo(String value) {
            addCriterion("modified_key =", value, "modifiedKey");
            return (Criteria) this;
        }

        public Criteria andModifiedKeyNotEqualTo(String value) {
            addCriterion("modified_key <>", value, "modifiedKey");
            return (Criteria) this;
        }

        public Criteria andModifiedKeyGreaterThan(String value) {
            addCriterion("modified_key >", value, "modifiedKey");
            return (Criteria) this;
        }

        public Criteria andModifiedKeyGreaterThanOrEqualTo(String value) {
            addCriterion("modified_key >=", value, "modifiedKey");
            return (Criteria) this;
        }

        public Criteria andModifiedKeyLessThan(String value) {
            addCriterion("modified_key <", value, "modifiedKey");
            return (Criteria) this;
        }

        public Criteria andModifiedKeyLessThanOrEqualTo(String value) {
            addCriterion("modified_key <=", value, "modifiedKey");
            return (Criteria) this;
        }

        public Criteria andModifiedKeyLike(String value) {
            addCriterion("modified_key like", value, "modifiedKey");
            return (Criteria) this;
        }

        public Criteria andModifiedKeyNotLike(String value) {
            addCriterion("modified_key not like", value, "modifiedKey");
            return (Criteria) this;
        }

        public Criteria andModifiedKeyIn(List<String> values) {
            addCriterion("modified_key in", values, "modifiedKey");
            return (Criteria) this;
        }

        public Criteria andModifiedKeyNotIn(List<String> values) {
            addCriterion("modified_key not in", values, "modifiedKey");
            return (Criteria) this;
        }

        public Criteria andModifiedKeyBetween(String value1, String value2) {
            addCriterion("modified_key between", value1, value2, "modifiedKey");
            return (Criteria) this;
        }

        public Criteria andModifiedKeyNotBetween(String value1, String value2) {
            addCriterion("modified_key not between", value1, value2, "modifiedKey");
            return (Criteria) this;
        }

        public Criteria andOldValueIsNull() {
            addCriterion("old_value is null");
            return (Criteria) this;
        }

        public Criteria andOldValueIsNotNull() {
            addCriterion("old_value is not null");
            return (Criteria) this;
        }

        public Criteria andOldValueEqualTo(String value) {
            addCriterion("old_value =", value, "oldValue");
            return (Criteria) this;
        }

        public Criteria andOldValueNotEqualTo(String value) {
            addCriterion("old_value <>", value, "oldValue");
            return (Criteria) this;
        }

        public Criteria andOldValueGreaterThan(String value) {
            addCriterion("old_value >", value, "oldValue");
            return (Criteria) this;
        }

        public Criteria andOldValueGreaterThanOrEqualTo(String value) {
            addCriterion("old_value >=", value, "oldValue");
            return (Criteria) this;
        }

        public Criteria andOldValueLessThan(String value) {
            addCriterion("old_value <", value, "oldValue");
            return (Criteria) this;
        }

        public Criteria andOldValueLessThanOrEqualTo(String value) {
            addCriterion("old_value <=", value, "oldValue");
            return (Criteria) this;
        }

        public Criteria andOldValueLike(String value) {
            addCriterion("old_value like", value, "oldValue");
            return (Criteria) this;
        }

        public Criteria andOldValueNotLike(String value) {
            addCriterion("old_value not like", value, "oldValue");
            return (Criteria) this;
        }

        public Criteria andOldValueIn(List<String> values) {
            addCriterion("old_value in", values, "oldValue");
            return (Criteria) this;
        }

        public Criteria andOldValueNotIn(List<String> values) {
            addCriterion("old_value not in", values, "oldValue");
            return (Criteria) this;
        }

        public Criteria andOldValueBetween(String value1, String value2) {
            addCriterion("old_value between", value1, value2, "oldValue");
            return (Criteria) this;
        }

        public Criteria andOldValueNotBetween(String value1, String value2) {
            addCriterion("old_value not between", value1, value2, "oldValue");
            return (Criteria) this;
        }

        public Criteria andNewValueIsNull() {
            addCriterion("new_value is null");
            return (Criteria) this;
        }

        public Criteria andNewValueIsNotNull() {
            addCriterion("new_value is not null");
            return (Criteria) this;
        }

        public Criteria andNewValueEqualTo(String value) {
            addCriterion("new_value =", value, "newValue");
            return (Criteria) this;
        }

        public Criteria andNewValueNotEqualTo(String value) {
            addCriterion("new_value <>", value, "newValue");
            return (Criteria) this;
        }

        public Criteria andNewValueGreaterThan(String value) {
            addCriterion("new_value >", value, "newValue");
            return (Criteria) this;
        }

        public Criteria andNewValueGreaterThanOrEqualTo(String value) {
            addCriterion("new_value >=", value, "newValue");
            return (Criteria) this;
        }

        public Criteria andNewValueLessThan(String value) {
            addCriterion("new_value <", value, "newValue");
            return (Criteria) this;
        }

        public Criteria andNewValueLessThanOrEqualTo(String value) {
            addCriterion("new_value <=", value, "newValue");
            return (Criteria) this;
        }

        public Criteria andNewValueLike(String value) {
            addCriterion("new_value like", value, "newValue");
            return (Criteria) this;
        }

        public Criteria andNewValueNotLike(String value) {
            addCriterion("new_value not like", value, "newValue");
            return (Criteria) this;
        }

        public Criteria andNewValueIn(List<String> values) {
            addCriterion("new_value in", values, "newValue");
            return (Criteria) this;
        }

        public Criteria andNewValueNotIn(List<String> values) {
            addCriterion("new_value not in", values, "newValue");
            return (Criteria) this;
        }

        public Criteria andNewValueBetween(String value1, String value2) {
            addCriterion("new_value between", value1, value2, "newValue");
            return (Criteria) this;
        }

        public Criteria andNewValueNotBetween(String value1, String value2) {
            addCriterion("new_value not between", value1, value2, "newValue");
            return (Criteria) this;
        }

        public Criteria andOldCommentIsNull() {
            addCriterion("old_comment is null");
            return (Criteria) this;
        }

        public Criteria andOldCommentIsNotNull() {
            addCriterion("old_comment is not null");
            return (Criteria) this;
        }

        public Criteria andOldCommentEqualTo(String value) {
            addCriterion("old_comment =", value, "oldComment");
            return (Criteria) this;
        }

        public Criteria andOldCommentNotEqualTo(String value) {
            addCriterion("old_comment <>", value, "oldComment");
            return (Criteria) this;
        }

        public Criteria andOldCommentGreaterThan(String value) {
            addCriterion("old_comment >", value, "oldComment");
            return (Criteria) this;
        }

        public Criteria andOldCommentGreaterThanOrEqualTo(String value) {
            addCriterion("old_comment >=", value, "oldComment");
            return (Criteria) this;
        }

        public Criteria andOldCommentLessThan(String value) {
            addCriterion("old_comment <", value, "oldComment");
            return (Criteria) this;
        }

        public Criteria andOldCommentLessThanOrEqualTo(String value) {
            addCriterion("old_comment <=", value, "oldComment");
            return (Criteria) this;
        }

        public Criteria andOldCommentLike(String value) {
            addCriterion("old_comment like", value, "oldComment");
            return (Criteria) this;
        }

        public Criteria andOldCommentNotLike(String value) {
            addCriterion("old_comment not like", value, "oldComment");
            return (Criteria) this;
        }

        public Criteria andOldCommentIn(List<String> values) {
            addCriterion("old_comment in", values, "oldComment");
            return (Criteria) this;
        }

        public Criteria andOldCommentNotIn(List<String> values) {
            addCriterion("old_comment not in", values, "oldComment");
            return (Criteria) this;
        }

        public Criteria andOldCommentBetween(String value1, String value2) {
            addCriterion("old_comment between", value1, value2, "oldComment");
            return (Criteria) this;
        }

        public Criteria andOldCommentNotBetween(String value1, String value2) {
            addCriterion("old_comment not between", value1, value2, "oldComment");
            return (Criteria) this;
        }

        public Criteria andNewCommentIsNull() {
            addCriterion("new_comment is null");
            return (Criteria) this;
        }

        public Criteria andNewCommentIsNotNull() {
            addCriterion("new_comment is not null");
            return (Criteria) this;
        }

        public Criteria andNewCommentEqualTo(String value) {
            addCriterion("new_comment =", value, "newComment");
            return (Criteria) this;
        }

        public Criteria andNewCommentNotEqualTo(String value) {
            addCriterion("new_comment <>", value, "newComment");
            return (Criteria) this;
        }

        public Criteria andNewCommentGreaterThan(String value) {
            addCriterion("new_comment >", value, "newComment");
            return (Criteria) this;
        }

        public Criteria andNewCommentGreaterThanOrEqualTo(String value) {
            addCriterion("new_comment >=", value, "newComment");
            return (Criteria) this;
        }

        public Criteria andNewCommentLessThan(String value) {
            addCriterion("new_comment <", value, "newComment");
            return (Criteria) this;
        }

        public Criteria andNewCommentLessThanOrEqualTo(String value) {
            addCriterion("new_comment <=", value, "newComment");
            return (Criteria) this;
        }

        public Criteria andNewCommentLike(String value) {
            addCriterion("new_comment like", value, "newComment");
            return (Criteria) this;
        }

        public Criteria andNewCommentNotLike(String value) {
            addCriterion("new_comment not like", value, "newComment");
            return (Criteria) this;
        }

        public Criteria andNewCommentIn(List<String> values) {
            addCriterion("new_comment in", values, "newComment");
            return (Criteria) this;
        }

        public Criteria andNewCommentNotIn(List<String> values) {
            addCriterion("new_comment not in", values, "newComment");
            return (Criteria) this;
        }

        public Criteria andNewCommentBetween(String value1, String value2) {
            addCriterion("new_comment between", value1, value2, "newComment");
            return (Criteria) this;
        }

        public Criteria andNewCommentNotBetween(String value1, String value2) {
            addCriterion("new_comment not between", value1, value2, "newComment");
            return (Criteria) this;
        }

        public Criteria andIsDeletedIsNull() {
            addCriterion("is_deleted is null");
            return (Criteria) this;
        }

        public Criteria andIsDeletedIsNotNull() {
            addCriterion("is_deleted is not null");
            return (Criteria) this;
        }

        public Criteria andIsDeletedEqualTo(Boolean value) {
            addCriterion("is_deleted =", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedNotEqualTo(Boolean value) {
            addCriterion("is_deleted <>", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedGreaterThan(Boolean value) {
            addCriterion("is_deleted >", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_deleted >=", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedLessThan(Boolean value) {
            addCriterion("is_deleted <", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedLessThanOrEqualTo(Boolean value) {
            addCriterion("is_deleted <=", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedIn(List<Boolean> values) {
            addCriterion("is_deleted in", values, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedNotIn(List<Boolean> values) {
            addCriterion("is_deleted not in", values, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedBetween(Boolean value1, Boolean value2) {
            addCriterion("is_deleted between", value1, value2, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedNotBetween(Boolean value1, Boolean value2) {
            addCriterion("is_deleted not between", value1, value2, "isDeleted");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}