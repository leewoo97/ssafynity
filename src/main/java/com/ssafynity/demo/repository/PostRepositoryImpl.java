package com.ssafynity.demo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafynity.demo.domain.Post;
import com.ssafynity.demo.domain.QPost;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Post> searchAdvanced(String keyword, String category, String sort, Pageable pageable) {
        QPost post = QPost.post;
        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.isBlank()) {
            builder.and(
                post.title.containsIgnoreCase(keyword)
                    .or(post.content.containsIgnoreCase(keyword))
            );
        }
        if (category != null && !category.isBlank()) {
            builder.and(post.category.eq(category));
        }

        OrderSpecifier<?> order = switch (sort == null ? "createdAt" : sort) {
            case "viewCount" -> post.viewCount.desc();
            case "likeCount" -> post.likeCount.desc();
            default          -> post.createdAt.desc();
        };

        List<Post> results = queryFactory
                .selectFrom(post)
                .where(builder)
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(post.count())
                .from(post)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(results, pageable, total != null ? total : 0L);
    }

    @Override
    public List<Post> findHotPosts(LocalDateTime since, int limit) {
        QPost post = QPost.post;
        return queryFactory
                .selectFrom(post)
                .where(post.createdAt.after(since))
                // 조회수 + 추천수*3 합산 점수로 정렬
                .orderBy(post.viewCount.add(post.likeCount.multiply(3)).desc())
                .limit(limit)
                .fetch();
    }
}
