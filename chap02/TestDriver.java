package chap02;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Money {
    public static final Money ZERO = Money.wons(0);
    private final BigDecimal amount;

    Money(BigDecimal amount) {
        this.amount = amount;
    }    
    public static Money wons(double amount) {
        return new Money(BigDecimal.valueOf(amount));
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
        return amount.equals(other.amount); 
    }
    public int hashCode() {
        return amount.hashCode();
    }
    public String toString() {
        return amount.toString() + "원";
    }
}
class Movie {
    private String title;
    private Duration runningTime;
    private Money fee;
    private DiscountPolicy discountPolicy; // 금액 할인 정책, 비율 할인 정책, None 할인 정책

    public Movie(String title, Duration runningTime, Money fee, DiscountPolicy discountPolicy) {
        this.title = title;
        this.runningTime = runningTime;
        this.fee = fee;
        this.discountPolicy = discountPolicy;
    }

    public Money getFee(int sequence, LocalDateTime whenScreened) {
        if (discountPolicy.isPossibleDC(sequence, whenScreened)) {
            return fee.minus(discountPolicy.getDC(fee));
        }
        return fee;
    }
}
class Screening {
    private Movie movie;
    private int sequence;
    private LocalDateTime whenScreened;
    private Money fee;

    public Screening (Movie movie, int sequence, LocalDateTime whenScreened) {
        this.movie = movie;
        this.sequence = sequence;
        this.whenScreened = whenScreened;
        
        this.fee = movie.getFee(sequence, whenScreened);
    }
    public Money getFee() {
        return fee;
    }
}

interface DiscountCondition {
    public boolean isPossibleDC(int sequence, LocalDateTime whenScreened);
}
class SequenceCondition implements DiscountCondition {
    private int sequence;
    public SequenceCondition (int sequence) {
        this.sequence = sequence;
    }
    @Override
    public boolean isPossibleDC(int sequence, LocalDateTime whenScreened) {
        return this.sequence == sequence;
    }
}
class PeriodCondition implements DiscountCondition {
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    public PeriodCondition (DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    @Override
    public boolean isPossibleDC(int sequence, LocalDateTime whenScreened) {
        return dayOfWeek.equals(whenScreened.getDayOfWeek()) &&
                startTime.compareTo(whenScreened.toLocalTime()) <= 0 &&
                endTime.compareTo(whenScreened.toLocalTime()) >= 0;
    }
    
}

abstract class DiscountPolicy {
    private List<DiscountCondition> conditions = new ArrayList<>();

    public DiscountPolicy (DiscountCondition ... c) {
        this.conditions = Arrays.asList(c);
    }

    abstract public Money getDC(Money fee);

    public boolean isPossibleDC(int sequence, LocalDateTime whenScreened) {
        for (DiscountCondition i : conditions) {
            if (i.isPossibleDC(sequence, whenScreened))
                return true;
        }
        return false;
    }
}

class AmountDiscountPolicy extends DiscountPolicy {
    Money dc;

    public AmountDiscountPolicy(Money dc, DiscountCondition ... c) {
        super(c);
        this.dc = dc;
    }
    @Override
    public Money getDC(Money fee) {
        return dc;
    }
}
class PercentDiscountPolicy extends DiscountPolicy {
    double percent;

    public PercentDiscountPolicy(double percent, DiscountCondition ... c) {
        super(c);
        this.percent = percent;
    }
    @Override
    public Money getDC(Money fee) {
        return fee.times(percent);
    }
}
class NoneDiscountPolicy extends DiscountPolicy {
    @Override
    public Money getDC(Money fee) {
        return Money.ZERO;
    }
}



class TestDriver {
    public static void main(String[] args) {
        
        Movie avatar = new Movie("아바타",
                                Duration.ofMinutes(120),
                                Money.wons(10000),
                                new AmountDiscountPolicy(Money.wons(1000),
                                                        new SequenceCondition(1),
                                                        new SequenceCondition(10),
                                                        new PeriodCondition(DayOfWeek.MONDAY, 
                                                                            LocalTime.of(10, 0), 
                                                                            LocalTime.of(11, 59))
        ));

        Movie titanic = new Movie("titanic", 
                                Duration.ofMinutes(100), 
                                Money.wons(5000), 
                                new PercentDiscountPolicy(0.1,
                                                        new PeriodCondition(DayOfWeek.THURSDAY, 
                                                                            LocalTime.of(10, 0), 
                                                                            LocalTime.of(11, 59)
        )));

        Screening s1 = new Screening(avatar, 1, LocalDateTime.of(2020, 1, 1, 10, 0));
        Screening s2 = new Screening(avatar, 2, LocalDateTime.of(2020, 1, 1, 11, 0));
        Screening s3 = new Screening(titanic, 1, LocalDateTime.of(2020, 1, 2, 10, 0));
        Screening s4 = new Screening(titanic, 2, LocalDateTime.of(2020, 1, 3, 11, 0));
        System.out.println(s1.getFee());
        System.out.println(s2.getFee());
        System.out.println(s3.getFee());
        System.out.println(s4.getFee());
    }
}

