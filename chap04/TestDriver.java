package chap04;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.math.BigDecimal;
import java.util.Objects;

/** BigDecimal 클래스를 이용한 사칙연산 메서드를 제공하는 Money 클래스 */
class Money {
    public static final Money ZERO = Money.wons(0);

    private final BigDecimal amount;

    public static Money wons(long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    public static Money wons(double amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    Money(BigDecimal amount) {
        this.amount = amount;
    }

    public Money plus(Money amount) {
        return new Money(this.amount.add(amount.amount));
    }

    public Money minus(Money amount) {
        return new Money(this.amount.subtract(amount.amount));
    }

    public Money times(double percent) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(percent)));
    }

    public boolean isLessThan(Money other) {
        return amount.compareTo(other.amount) < 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return amount.compareTo(other.amount) >= 0;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Money)) {
            return false;
        }

        Money other = (Money)object;
        return Objects.equals(amount.doubleValue(), other.amount.doubleValue());
    }

    public int hashCode() {
        return Objects.hashCode(amount);
    }

    public String toString() {
        return amount.toString() + "원";
    }
}

class Customer {
    private String name;
    private String id;

    public Customer(String name, String id) {
        this.id = id;
        this.name = name;
    }
}

enum MovieType {
    AMOUNT_DISCOUNT,    // 금액 할인 정책
    PERCENT_DISCOUNT,   // 비율 할인 정책
    NONE_DISCOUNT       // 미적용
}

enum DiscountConditionType {
    SEQUENCE,       // 순번조건
    PERIOD          // 기간 조건
}

class DiscountCondition {
    private DiscountConditionType type;

    private int sequence;

    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public DiscountCondition(DiscountConditionType type, int sequence) {
        if (type != DiscountConditionType.SEQUENCE) {
            throw new IllegalArgumentException();
        }

        this.type = type;
        this.sequence = sequence;   
    }

    public DiscountCondition(DiscountConditionType type, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        if (type != DiscountConditionType.PERIOD) {
            throw new IllegalArgumentException();
        }

        this.type = type;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /** 기간 조건이 만족되는지 확인 */
    public boolean isDiscountable(DayOfWeek dayOfWeek, LocalTime time) {
        if (type != DiscountConditionType.PERIOD) {
            throw new IllegalArgumentException();
        }
        return this.dayOfWeek.equals(dayOfWeek) &&
                this.startTime.compareTo(time) <= 0 &&
                this.endTime.compareTo(time) >= 0;
    }

    /** 순번 조건이 만족되는지 확인 */
    public boolean isDiscountable(int sequence) {
        if (type != DiscountConditionType.SEQUENCE) {
            throw new IllegalArgumentException();
        }
        return this.sequence == sequence;
    }

    public DiscountConditionType getType() {
        return type;
    }
}

class Movie {
    private String title;
    private Duration runningTime;
    private Money fee; // 기본 요금 : 할인이 적용된 요금을 말하는 것이 아니다.
    private List<DiscountCondition> discountConditions; // 할인 조건 목록

    private MovieType movieType; // 할인 정책 : 비율 할인 정책, 금액 할인 정책, 미적용
    private Money discountAmount; // 할인 금액
    private double discountPercent; // 할인 비율 : 0 ~ 1 사이의 값
    
    /** 비율 할인 정책을 위한 생성자 */
    public Movie(String title, Duration runningTime, Money fee, double discountPercent, DiscountCondition... discountConditions) {
        this(MovieType.PERCENT_DISCOUNT, title, runningTime, fee, Money.ZERO, discountPercent, discountConditions);
    }

    /** 금액 할인 정책을 위한 생성자 */
    public Movie(String title, Duration runningTime, Money fee, Money discountAmount, DiscountCondition... discountConditions) {
        this(MovieType.AMOUNT_DISCOUNT, title, runningTime, fee, discountAmount, 0, discountConditions);
    }

    /** 할인 정책이 없는 경우 생성자 */
    public Movie(String title, Duration runningTime, Money fee) {
        this(MovieType.NONE_DISCOUNT, title, runningTime, fee, Money.ZERO, 0);
    }

    private Movie(MovieType movieType, String title, Duration runningTime, Money fee, Money discountAmount, double discountPercent,
                  DiscountCondition... discountConditions) {
        this.movieType = movieType;
        this.title = title;
        this.runningTime = runningTime;
        this.fee = fee;
        this.discountAmount = discountAmount;
        this.discountPercent = discountPercent;
        this.discountConditions = Arrays.asList(discountConditions);
    }


    public Money calculateAmountDiscountedFee() {
        if (movieType != MovieType.AMOUNT_DISCOUNT) {
            throw new IllegalArgumentException();
        }
        return fee.minus(discountAmount);
    }

    public Money calculatePercentDiscountedFee() {
        if (movieType != MovieType.PERCENT_DISCOUNT) {
            throw new IllegalArgumentException();
        }
        return fee.minus(fee.times(discountPercent));
    }

    public Money calculateNoneDiscountedFee() {
        if (movieType != MovieType.NONE_DISCOUNT) {
            throw new IllegalArgumentException();
        }
        return fee;
    }

    public boolean isDiscountable(LocalDateTime whenScreened, int sequence) {
        for (DiscountCondition condition : discountConditions) {
            if (condition.getType() == DiscountConditionType.PERIOD) { // 기간 조건
                if (condition.isDiscountable(whenScreened.getDayOfWeek(), whenScreened.toLocalTime())) {
                    return true;
                }
            } else {
                if (condition.isDiscountable(sequence)) { // 순번 조건
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return  "Movie{" + '\n' +
                "title='" + title + '\n' +
                ", runningTime=" + runningTime + '\n' +
                ", fee=" + fee + '\n' +
                ", movieType=" + movieType + '\n' +
                ", discountAmount=" + discountAmount + '\n' +
                ", discountPercent=" + discountPercent + '\n';
    }

    public Money getFee(int sequence, LocalDateTime whenScreened) {
        switch (movieType) {
            case AMOUNT_DISCOUNT:
                if (isDiscountable(whenScreened, sequence)) {
                    return calculateAmountDiscountedFee();
                }
                break;
            case PERCENT_DISCOUNT:
                if (isDiscountable(whenScreened, sequence)) {
                    return calculatePercentDiscountedFee();
                }
                break;
            case NONE_DISCOUNT:
                return calculateNoneDiscountedFee();
        }
        return calculateNoneDiscountedFee();
    }
}

class Screening {
    private Movie movie;
    private int sequence;
    private LocalDateTime whenScreened;
    private Money fee;

    public Screening(Movie movie, int sequence, LocalDateTime whenScreened) {
        this.movie = movie;
        this.sequence = sequence;
        this.whenScreened = whenScreened;

        this.fee = movie.getFee(sequence, whenScreened);
    }

    public Money getFee() {
        return fee;
    }

    @Override
    public String toString() {
        return "Screening{" +
                "movie=" + movie.toString() +
                ", sequence=" + sequence +
                ", whenScreened=" + whenScreened +
                '}';
    }
}

class Reservation {
    private Customer customer; 
    private Screening screening;
    private Money fee; 
    private int audienceCount; 

    public Reservation(Customer customer, Screening screening, int audienceCount) {
        this.customer = customer;
        this.screening = screening;
        this.fee = screening.getFee().times(audienceCount);
        this.audienceCount = audienceCount;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "customer=" + customer +
                ", screening=" + screening.toString() +
                ", fee=" + fee +
                ", audienceCount=" + audienceCount +
                '}';
    }

}

public class TestDriver {
    public static void main(String[] args) {
        Movie avatar = new Movie(
                "아바타",
                Duration.ofMinutes(120),
                Money.wons(10000),
                Money.wons(1000), // 금액 할인 정책
                new DiscountCondition(DiscountConditionType.SEQUENCE, 1),
                new DiscountCondition(DiscountConditionType.SEQUENCE, 10),
                new DiscountCondition(DiscountConditionType.PERIOD, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 59)),
                new DiscountCondition(DiscountConditionType.PERIOD, DayOfWeek.THURSDAY, LocalTime.of(10, 0), LocalTime.of(20, 59))
        );

        Movie titanic = new Movie(
                "타이타닉",
                Duration.ofMinutes(180),
                Money.wons(11000),
                Money.wons(1000), // 금액 할인 정책
                new DiscountCondition(DiscountConditionType.SEQUENCE, 2),
                new DiscountCondition(DiscountConditionType.SEQUENCE, 11),
                new DiscountCondition(DiscountConditionType.PERIOD, DayOfWeek.TUESDAY, LocalTime.of(14, 0), LocalTime.of(16, 59)),
                new DiscountCondition(DiscountConditionType.PERIOD, DayOfWeek.THURSDAY, LocalTime.of(10, 0), LocalTime.of(13, 59))
        );

        Movie starWars = new Movie(
                "스타워즈",
                Duration.ofMinutes(210),
                Money.wons(10000),
                0.1, // 비율 할인 정책
                new DiscountCondition(DiscountConditionType.SEQUENCE, 3),
                new DiscountCondition(DiscountConditionType.SEQUENCE, 12),
                new DiscountCondition(DiscountConditionType.PERIOD, DayOfWeek.WEDNESDAY, LocalTime.of(10, 0), LocalTime.of(13, 59)),
                new DiscountCondition(DiscountConditionType.PERIOD, DayOfWeek.FRIDAY, LocalTime.of(10, 0), LocalTime.of(20, 59))
        );

        Money m1 = avatar.calculateAmountDiscountedFee();
        // Money m2 = avatar.calculatePercentDiscountedFee(); // IllegalArgumentException
        // Money m3 = avatar.calculateNoneDiscountedFee(); // IllegalArgumentException

        // 영화 예매
        Screening screening = new Screening(avatar, 1, LocalDateTime.of(2020, 10, 1, 10, 0));
        Customer customer = new Customer("customer", "7777-7777");
        Reservation reservation = new Reservation(customer, screening, 1);

        System.out.println(reservation);
    }
}

/*
    TestDriver
        ReservationAgency - reserve
            Screening - calculateFee
                Movie - getMovieType, 
                        isDiscountable, 
                            DiscountCondition - getType, isDiscountable
                        calculateAmountDiscountedFee
                        calculatePercentDiscountedFee
                        calculateNoneDiscountedFee
                            Money - minus, times
*/