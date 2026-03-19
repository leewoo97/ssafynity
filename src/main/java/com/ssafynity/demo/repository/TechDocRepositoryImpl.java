package com.ssafynity.demo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafynity.demo.domain.QTechDoc;
import com.ssafynity.demo.domain.TechDoc;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class TechDocRepositoryImpl implements TechDocRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<TechDoc> searchDocs(String keyword, String category, Pageable pageable) {
        QTechDoc doc = QTechDoc.techDoc;
        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.isBlank()) {
            builder.and(
                doc.title.containsIgnoreCase(keyword)
                    .or(doc.content.containsIgnoreCase(keyword))
                    .or(doc.tags.containsIgnoreCase(keyword))
            );
        }
        if (category != null && !category.isBlank()) {
            builder.and(doc.category.eq(category));
        }

        List<TechDoc> results = queryFactory
                .selectFrom(doc)
                .where(builder)
                .orderBy(doc.pinned.desc(), doc.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(doc.count())
                .from(doc)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(results, pageable, total != null ? total : 0L);
    }
}
