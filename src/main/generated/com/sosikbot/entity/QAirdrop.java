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

    public final StringPath content = createString("content");

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final StringPath exchange = createString("exchange");

    public final NumberPath<Integer> no = createNumber("no", Integer.class);

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final StringPath title = createString("title");

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

