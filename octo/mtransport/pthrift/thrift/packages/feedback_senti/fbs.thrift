struct FSLabel {
    1: required string label,
    2: required i32 isPositive,
    3: required i32 count,
}

struct FSItem {
    1: required i64 fsItemId,
    2: required i64 feedbackId,
    3: required i32 startPos,
    4: required i32 endPos,
    5: required string phrase,
}

struct FSRichLabel {
    1: required string label,
    2: required i32 isPositive,
    3: required i32 count,
    4: required list<FSItem> feedbacks,
}

service FSQuery {
    list<FSLabel> getLablesByDeal(1: i64 dealId)
    list<FSItem> getFeedbacksByDealLabel(1: i64 dealId, 2: string label)

    list<FSRichLabel> getRichLabelsByDeal(1: i64 dealId)
    i32 deleteFSItem(1: i64 dealId, 2: i64 fsItemId)

    // TODO
    list<FSLabel> getLabelsByPoi(1: i64 poiId)
    list<FSItem> getFeedbacksByPoiLabel(1: i64 poiId, 2: string label)
}