package com.sosikbot.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QLaunchPool is a Querydsl query type for LaunchPool
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLaunchPool extends EntityPathBase<LaunchPool> {

    private static final long serialVersionUID = -209178041L;

    public static final QLaunchPool launchPool = new QLaunchPool("launchPool");

    public final DateTimePath<java.time.LocalDateTime> endDate = createDateTime("endDate", java.time.LocalDateTime.class);

    public final StringPath exchange = createString("exchange");

    public final StringPath launchNo = createString("launchNo");

    public final DateTimePath<java.time.LocalDateTime> startDate = createDateTime("startDate", java.time.LocalDateTime.class);

    public final StringPath status = createString("status");

    public final StringPath title = createString("title");

    public QLaunchPool(String variable) {
        super(LaunchPool.class, forVariable(variable));
    }

    public QLaunchPool(Path<? extends LaunchPool> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLaunchPool(PathMetadata metadata) {
        super(LaunchPool.class, metadata);
    }

}

