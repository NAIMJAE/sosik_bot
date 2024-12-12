package com.sosikbot.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPoolDetail is a Querydsl query type for PoolDetail
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPoolDetail extends EntityPathBase<PoolDetail> {

    private static final long serialVersionUID = -765973659L;

    public static final QPoolDetail poolDetail = new QPoolDetail("poolDetail");

    public final StringPath launchNo = createString("launchNo");

    public final StringPath maximum = createString("maximum");

    public final StringPath minimum = createString("minimum");

    public final StringPath name = createString("name");

    public final NumberPath<Integer> poolNo = createNumber("poolNo", Integer.class);

    public final StringPath total = createString("total");

    public QPoolDetail(String variable) {
        super(PoolDetail.class, forVariable(variable));
    }

    public QPoolDetail(Path<? extends PoolDetail> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPoolDetail(PathMetadata metadata) {
        super(PoolDetail.class, metadata);
    }

}

