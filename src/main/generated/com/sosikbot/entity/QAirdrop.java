package com.sosikbot.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAirdrop is a Querydsl query type for Airdrop
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAirdrop extends EntityPathBase<Airdrop> {

    private static final long serialVersionUID = 625684065L;

    public static final QAirdrop airdrop = new QAirdrop("airdrop");

    public final NumberPath<Double> actualReward_coin = createNumber("actualReward_coin", Double.class);

    public final NumberPath<Double> actualReward_krw = createNumber("actualReward_krw", Double.class);

    public final StringPath coin = createString("coin");

    public final StringPath content = createString("content");

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final StringPath exchange = createString("exchange");

    public final NumberPath<Integer> no = createNumber("no", Integer.class);

    public final StringPath noticeURL = createString("noticeURL");

    public final DatePath<java.time.LocalDate> paymentDate = createDate("paymentDate", java.time.LocalDate.class);

    public final StringPath rewardUnit = createString("rewardUnit");

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final StringPath title = createString("title");

    public final NumberPath<Integer> totalReward = createNumber("totalReward", Integer.class);

    public QAirdrop(String variable) {
        super(Airdrop.class, forVariable(variable));
    }

    public QAirdrop(Path<? extends Airdrop> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAirdrop(PathMetadata metadata) {
        super(Airdrop.class, metadata);
    }

}

