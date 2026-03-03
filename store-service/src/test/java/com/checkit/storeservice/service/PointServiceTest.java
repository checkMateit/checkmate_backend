package com.checkit.storeservice.service;

import com.checkit.storeservice.entity.PointTransactionEntity;
import com.checkit.storeservice.repository.PointTransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PointService 단위 테스트")
class PointServiceTest {

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @InjectMocks
    private PointService pointService;

    private static final UUID USER_ID = UUID.randomUUID();

    @Nested
    @DisplayName("getCurrentBalance")
    class GetCurrentBalance {

        @Test
        void 거래_없으면_0_반환() {
            when(pointTransactionRepository.findFirstByUserIdOrderByCreatedAtDesc(USER_ID))
                    .thenReturn(Optional.empty());

            int balance = pointService.getCurrentBalance(USER_ID);

            assertThat(balance).isZero();
        }

        @Test
        void 최근_거래_잔액_반환() {
            PointTransactionEntity last = PointTransactionEntity.builder()
                    .userId(USER_ID)
                    .type("적립")
                    .amount(100)
                    .balanceAfter(500)
                    .description("테스트")
                    .build();
            when(pointTransactionRepository.findFirstByUserIdOrderByCreatedAtDesc(USER_ID))
                    .thenReturn(Optional.of(last));

            int balance = pointService.getCurrentBalance(USER_ID);

            assertThat(balance).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("earnPoint")
    class EarnPoint {

        @Test
        void 금액이_0_이하면_IllegalArgumentException() {
            assertThatThrownBy(() -> pointService.earnPoint(USER_ID, 0, "테스트"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("적립 금액은 0보다 커야 합니다");

            assertThatThrownBy(() -> pointService.earnPoint(USER_ID, -1, "테스트"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void 성공_시_recordTransaction_호출() {
            when(pointTransactionRepository.findFirstByUserIdOrderByCreatedAtDesc(USER_ID))
                    .thenReturn(Optional.empty());
            when(pointTransactionRepository.save(any(PointTransactionEntity.class))).thenAnswer(i -> i.getArgument(0));

            pointService.earnPoint(USER_ID, 100, "리뷰 적립");

            verify(pointTransactionRepository).save(any(PointTransactionEntity.class));
        }
    }

    @Nested
    @DisplayName("spendPoint")
    class SpendPoint {

        @Test
        void 금액이_0_이하면_IllegalArgumentException() {
            assertThatThrownBy(() -> pointService.spendPoint(USER_ID, 0, "테스트"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사용 금액은 0보다 커야 합니다");
        }

        @Test
        void 잔액_부족_시_RuntimeException() {
            when(pointTransactionRepository.findFirstByUserIdOrderByCreatedAtDesc(USER_ID))
                    .thenReturn(Optional.of(PointTransactionEntity.builder()
                            .userId(USER_ID)
                            .type("적립")
                            .amount(50)
                            .balanceAfter(50)
                            .description("적립")
                            .build()));

            assertThatThrownBy(() -> pointService.spendPoint(USER_ID, 100, "사용"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("포인트가 부족합니다");
        }
    }

    @Nested
    @DisplayName("getPointHistory")
    class GetPointHistory {

        @Test
        void type_null_이면_전체_조회() {
            Pageable pageable = PageRequest.of(0, 10);
            when(pointTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(USER_ID, pageable))
                    .thenReturn(new PageImpl<>(List.of()));

            pointService.getPointHistory(USER_ID, null, pageable);

            verify(pointTransactionRepository).findAllByUserIdOrderByCreatedAtDesc(USER_ID, pageable);
        }

        @Test
        void type_전체_이면_전체_조회() {
            Pageable pageable = PageRequest.of(0, 10);
            when(pointTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(USER_ID, pageable))
                    .thenReturn(new PageImpl<>(List.of()));

            pointService.getPointHistory(USER_ID, "전체", pageable);

            verify(pointTransactionRepository).findAllByUserIdOrderByCreatedAtDesc(USER_ID, pageable);
        }

        @Test
        void type_지정_시_타입별_조회() {
            Pageable pageable = PageRequest.of(0, 10);
            when(pointTransactionRepository.findAllByUserIdAndTypeOrderByCreatedAtDesc(USER_ID, "적립", pageable))
                    .thenReturn(new PageImpl<>(List.of()));

            pointService.getPointHistory(USER_ID, "적립", pageable);

            verify(pointTransactionRepository).findAllByUserIdAndTypeOrderByCreatedAtDesc(USER_ID, "적립", pageable);
        }
    }
}
