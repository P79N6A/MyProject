package com.sankuai.octo.errorlog.model;

import java.util.ArrayList;
import java.util.List;

public class LogAlarmSeverityConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public LogAlarmSeverityConfigExample() {
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

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Integer value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Integer value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Integer value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Integer value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Integer value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Integer> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Integer> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Integer value1, Integer value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Integer value1, Integer value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andAppkeyIsNull() {
            addCriterion("appkey is null");
            return (Criteria) this;
        }

        public Criteria andAppkeyIsNotNull() {
            addCriterion("appkey is not null");
            return (Criteria) this;
        }

        public Criteria andAppkeyEqualTo(String value) {
            addCriterion("appkey =", value, "appkey");
            return (Criteria) this;
        }

        public Criteria andAppkeyNotEqualTo(String value) {
            addCriterion("appkey <>", value, "appkey");
            return (Criteria) this;
        }

        public Criteria andAppkeyGreaterThan(String value) {
            addCriterion("appkey >", value, "appkey");
            return (Criteria) this;
        }

        public Criteria andAppkeyGreaterThanOrEqualTo(String value) {
            addCriterion("appkey >=", value, "appkey");
            return (Criteria) this;
        }

        public Criteria andAppkeyLessThan(String value) {
            addCriterion("appkey <", value, "appkey");
            return (Criteria) this;
        }

        public Criteria andAppkeyLessThanOrEqualTo(String value) {
            addCriterion("appkey <=", value, "appkey");
            return (Criteria) this;
        }

        public Criteria andAppkeyLike(String value) {
            addCriterion("appkey like", value, "appkey");
            return (Criteria) this;
        }

        public Criteria andAppkeyNotLike(String value) {
            addCriterion("appkey not like", value, "appkey");
            return (Criteria) this;
        }

        public Criteria andAppkeyIn(List<String> values) {
            addCriterion("appkey in", values, "appkey");
            return (Criteria) this;
        }

        public Criteria andAppkeyNotIn(List<String> values) {
            addCriterion("appkey not in", values, "appkey");
            return (Criteria) this;
        }

        public Criteria andAppkeyBetween(String value1, String value2) {
            addCriterion("appkey between", value1, value2, "appkey");
            return (Criteria) this;
        }

        public Criteria andAppkeyNotBetween(String value1, String value2) {
            addCriterion("appkey not between", value1, value2, "appkey");
            return (Criteria) this;
        }

        public Criteria andOkIsNull() {
            addCriterion("ok is null");
            return (Criteria) this;
        }

        public Criteria andOkIsNotNull() {
            addCriterion("ok is not null");
            return (Criteria) this;
        }

        public Criteria andOkEqualTo(Integer value) {
            addCriterion("ok =", value, "ok");
            return (Criteria) this;
        }

        public Criteria andOkNotEqualTo(Integer value) {
            addCriterion("ok <>", value, "ok");
            return (Criteria) this;
        }

        public Criteria andOkGreaterThan(Integer value) {
            addCriterion("ok >", value, "ok");
            return (Criteria) this;
        }

        public Criteria andOkGreaterThanOrEqualTo(Integer value) {
            addCriterion("ok >=", value, "ok");
            return (Criteria) this;
        }

        public Criteria andOkLessThan(Integer value) {
            addCriterion("ok <", value, "ok");
            return (Criteria) this;
        }

        public Criteria andOkLessThanOrEqualTo(Integer value) {
            addCriterion("ok <=", value, "ok");
            return (Criteria) this;
        }

        public Criteria andOkIn(List<Integer> values) {
            addCriterion("ok in", values, "ok");
            return (Criteria) this;
        }

        public Criteria andOkNotIn(List<Integer> values) {
            addCriterion("ok not in", values, "ok");
            return (Criteria) this;
        }

        public Criteria andOkBetween(Integer value1, Integer value2) {
            addCriterion("ok between", value1, value2, "ok");
            return (Criteria) this;
        }

        public Criteria andOkNotBetween(Integer value1, Integer value2) {
            addCriterion("ok not between", value1, value2, "ok");
            return (Criteria) this;
        }

        public Criteria andWarningIsNull() {
            addCriterion("warning is null");
            return (Criteria) this;
        }

        public Criteria andWarningIsNotNull() {
            addCriterion("warning is not null");
            return (Criteria) this;
        }

        public Criteria andWarningEqualTo(Integer value) {
            addCriterion("warning =", value, "warning");
            return (Criteria) this;
        }

        public Criteria andWarningNotEqualTo(Integer value) {
            addCriterion("warning <>", value, "warning");
            return (Criteria) this;
        }

        public Criteria andWarningGreaterThan(Integer value) {
            addCriterion("warning >", value, "warning");
            return (Criteria) this;
        }

        public Criteria andWarningGreaterThanOrEqualTo(Integer value) {
            addCriterion("warning >=", value, "warning");
            return (Criteria) this;
        }

        public Criteria andWarningLessThan(Integer value) {
            addCriterion("warning <", value, "warning");
            return (Criteria) this;
        }

        public Criteria andWarningLessThanOrEqualTo(Integer value) {
            addCriterion("warning <=", value, "warning");
            return (Criteria) this;
        }

        public Criteria andWarningIn(List<Integer> values) {
            addCriterion("warning in", values, "warning");
            return (Criteria) this;
        }

        public Criteria andWarningNotIn(List<Integer> values) {
            addCriterion("warning not in", values, "warning");
            return (Criteria) this;
        }

        public Criteria andWarningBetween(Integer value1, Integer value2) {
            addCriterion("warning between", value1, value2, "warning");
            return (Criteria) this;
        }

        public Criteria andWarningNotBetween(Integer value1, Integer value2) {
            addCriterion("warning not between", value1, value2, "warning");
            return (Criteria) this;
        }

        public Criteria andErrorIsNull() {
            addCriterion("error is null");
            return (Criteria) this;
        }

        public Criteria andErrorIsNotNull() {
            addCriterion("error is not null");
            return (Criteria) this;
        }

        public Criteria andErrorEqualTo(Integer value) {
            addCriterion("error =", value, "error");
            return (Criteria) this;
        }

        public Criteria andErrorNotEqualTo(Integer value) {
            addCriterion("error <>", value, "error");
            return (Criteria) this;
        }

        public Criteria andErrorGreaterThan(Integer value) {
            addCriterion("error >", value, "error");
            return (Criteria) this;
        }

        public Criteria andErrorGreaterThanOrEqualTo(Integer value) {
            addCriterion("error >=", value, "error");
            return (Criteria) this;
        }

        public Criteria andErrorLessThan(Integer value) {
            addCriterion("error <", value, "error");
            return (Criteria) this;
        }

        public Criteria andErrorLessThanOrEqualTo(Integer value) {
            addCriterion("error <=", value, "error");
            return (Criteria) this;
        }

        public Criteria andErrorIn(List<Integer> values) {
            addCriterion("error in", values, "error");
            return (Criteria) this;
        }

        public Criteria andErrorNotIn(List<Integer> values) {
            addCriterion("error not in", values, "error");
            return (Criteria) this;
        }

        public Criteria andErrorBetween(Integer value1, Integer value2) {
            addCriterion("error between", value1, value2, "error");
            return (Criteria) this;
        }

        public Criteria andErrorNotBetween(Integer value1, Integer value2) {
            addCriterion("error not between", value1, value2, "error");
            return (Criteria) this;
        }

        public Criteria andDisasterIsNull() {
            addCriterion("disaster is null");
            return (Criteria) this;
        }

        public Criteria andDisasterIsNotNull() {
            addCriterion("disaster is not null");
            return (Criteria) this;
        }

        public Criteria andDisasterEqualTo(Integer value) {
            addCriterion("disaster =", value, "disaster");
            return (Criteria) this;
        }

        public Criteria andDisasterNotEqualTo(Integer value) {
            addCriterion("disaster <>", value, "disaster");
            return (Criteria) this;
        }

        public Criteria andDisasterGreaterThan(Integer value) {
            addCriterion("disaster >", value, "disaster");
            return (Criteria) this;
        }

        public Criteria andDisasterGreaterThanOrEqualTo(Integer value) {
            addCriterion("disaster >=", value, "disaster");
            return (Criteria) this;
        }

        public Criteria andDisasterLessThan(Integer value) {
            addCriterion("disaster <", value, "disaster");
            return (Criteria) this;
        }

        public Criteria andDisasterLessThanOrEqualTo(Integer value) {
            addCriterion("disaster <=", value, "disaster");
            return (Criteria) this;
        }

        public Criteria andDisasterIn(List<Integer> values) {
            addCriterion("disaster in", values, "disaster");
            return (Criteria) this;
        }

        public Criteria andDisasterNotIn(List<Integer> values) {
            addCriterion("disaster not in", values, "disaster");
            return (Criteria) this;
        }

        public Criteria andDisasterBetween(Integer value1, Integer value2) {
            addCriterion("disaster between", value1, value2, "disaster");
            return (Criteria) this;
        }

        public Criteria andDisasterNotBetween(Integer value1, Integer value2) {
            addCriterion("disaster not between", value1, value2, "disaster");
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