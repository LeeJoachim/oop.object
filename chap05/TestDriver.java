package chap05;
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
    public String toString() {
        return  "==Customer info==" + '\n' +
                "name='" + name + '\n' +
                "id='" + id + '\n';
    }
}
/** 할인 가능 여부를 판단하는 isDiscountable() 메서드를 가진 인터페이스 */
interface DCCondition {
    boolean isDiscountable(int sequence, LocalDateTime whenScreened);
}
class sequenceDCCondition implements DCCondition {
    private int sequence;

    public sequenceDCCondition(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean isDiscountable(int sequence, LocalDateTime whenScreened) {
        return this.sequence == sequence;
    }
}
class periodDCCondition implements DCCondition {
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public periodDCCondition(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public boolean isDiscountable(int sequence, LocalDateTime whenScreened) {
        return this.dayOfWeek.equals(whenScreened.getDayOfWeek()) &&
                this.startTime.compareTo(whenScreened.toLocalTime()) <= 0 &&
                this.endTime.compareTo(whenScreened.toLocalTime()) >= 0;
    }
}
/** 할인 요금을 리턴하는 getFee() 메서드를 가진 추상 클래스 */
abstract class DCPolicy {
    private List<DCCondition> dcConditions; // 할인 조건 목록

    public DCPolicy(DCCondition... dcConditions) {
        this.dcConditions = Arrays.asList(dcConditions);
    }

    public boolean isDiscountable(int sequence, LocalDateTime whenScreened) {
        for (DCCondition i : dcConditions) {
            if (i.isDiscountable(sequence, whenScreened)) {
                return true;
            }
        }
        return false;
    }

    abstract Money getFee(Money fee);


}
class AmountDCPolicy extends DCPolicy {
    Money DCAmount;
    public AmountDCPolicy(Money DCAmount, DCCondition... dcConditions) {
        super(dcConditions);
        this.DCAmount = DCAmount;
    }
    @Override
    Money getFee(Money fee) {
        return fee.minus(DCAmount);
    }
    public String toString() {
        return "AmountDCPolicy";
    }
    
}
class PercentDCPolicy extends DCPolicy {
    double DCPercent;
    public PercentDCPolicy(double dcPercent, DCCondition... dcConditions) {
        super(dcConditions);
        this.DCPercent = dcPercent;
    }
    @Override
    Money getFee(Money fee) {
        return fee.minus(fee.times(DCPercent));
    }
    public String toString() {
        return "PercentDCPolicy";
    }
}

class Movie {
    private String title;
    private Duration runningTime;
    private Money fee; // 기본 요금 : 할인이 적용된 요금을 말하는 것이 아니다.
    private DCPolicy dcPolicy; // xxx
   
    public Movie(String title, Duration runningTime, Money fee, DCPolicy dcPolicy) {
        this.title = title;
        this.runningTime = runningTime;
        this.fee = fee;
        this.dcPolicy = dcPolicy;
    }

    @Override
    public String toString() {
        return  "==Movie info==" + '\n' +
                "title='" + title + '\n' +
                "runningTime=" + runningTime + '\n' +
                "fee=" + fee + '\n' +
                "dcPolicy=" + dcPolicy + '\n';
    }

    public Money getFee(int sequence, LocalDateTime whenScreened) {
        
        if (dcPolicy.isDiscountable(sequence, whenScreened)) {
            return dcPolicy.getFee(this.fee);
        } else {
            return this.fee;
        }
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
        return "" + movie + '\n' +
                "==Screening info==" + '\n' +
                "sequence=" + sequence + '\n' +
                "whenScreened=" + whenScreened + '\n' +
                "fee(after DC)=" + fee + '\n';
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
        return  "" + 
                customer + '\n' +
                screening + '\n' +
        
                "==Reservation info==" + '\n' +
                "fee=" + fee + '\n' +
                "audienceCount=" + audienceCount + '\n';
    }

}

public class TestDriver {
    public static void main(String[] args) {
        // xxx
        Movie avatar2 = new Movie(
            "아바타2",
            Duration.ofMinutes(125),
            Money.wons(10000),
            new AmountDCPolicy(Money.wons(1000),
                new sequenceDCCondition(1), 
                new sequenceDCCondition(10), 
                new periodDCCondition(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 59)), 
                new periodDCCondition(DayOfWeek.THURSDAY, LocalTime.of(10, 0), LocalTime.of(20, 59)))
        );
        
        Screening scr2 = new Screening(avatar2, 2, LocalDateTime.of(2020, 10, 1, 10, 00));
        Customer customer = new Customer("kim", "7777-7777");
        Reservation reservation = new Reservation(customer, scr2, 2);

        System.out.println(reservation);
        
    }
}