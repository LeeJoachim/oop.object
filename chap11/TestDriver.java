package chap11;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

class PhoneTime {
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public PhoneTime(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Duration getLength () {
        return Duration.between(startTime, endTime);
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }
}

class PhonePolicyManager {
    private List<PhoneTime> phoneTimes = new ArrayList<>();
    private PhonePolicy phonePolicy;

    public PhonePolicyManager(PhonePolicy phonePolicy) {
        this.phonePolicy = phonePolicy;
    }
    public void addPhoneTimesRecord(PhoneTime phoneTime) {
        phoneTimes.add(phoneTime);
    }
    public List<PhoneTime> getPhoneTimes() {
        return Collections.unmodifiableList(phoneTimes);
    }
    public void setPhonePolicy(PhonePolicy phonePolicy) {
        this.phonePolicy = phonePolicy;
    }
    public Money calculateFee() {
        return phonePolicy.calculateFee(this);
    }

}

interface PhonePolicy {
    Money calculateFee(PhonePolicyManager phonePolicyManager);
}

abstract class BasicRatePolicy implements PhonePolicy {

    public Money calculateFee(PhonePolicyManager phonePolicyManager) {
        Money result = Money.ZERO;
        for (PhoneTime phoneTime : phonePolicyManager.getPhoneTimes()) {
            result = result.plus(calc(phoneTime));
        }
        return result;
    }

    abstract Money calc(PhoneTime phoneTime);
}

class RegularPolicy extends BasicRatePolicy {
    private Money amount;
    private Duration seconds;

    public RegularPolicy(Money amount, Duration seconds) {
        this.amount = amount;
        this.seconds = seconds;
    }

    Money calc(PhoneTime phoneTime) {
        return amount.times(phoneTime.getLength().getSeconds() / seconds.getSeconds());
    }
}

class NightlyDiscountPolicy extends BasicRatePolicy {
    private Money nightlyAmount;
    private Money regularAmount;
    private Duration seconds;

    public NightlyDiscountPolicy(Money nightlyAmount, Money regularAmount, Duration seconds) {
        this.nightlyAmount = nightlyAmount;
        this.regularAmount = regularAmount;
        this.seconds = seconds;
    }

    Money calc(PhoneTime phoneTime) {
        if (phoneTime.getStartTime().getHour() >= 22) {
            return nightlyAmount.times(phoneTime.getLength().getSeconds() / seconds.getSeconds());
        }
        return regularAmount.times(phoneTime.getLength().getSeconds() / seconds.getSeconds());
    }
}

abstract class AdditionalRatePolicy implements PhonePolicy {
    private PhonePolicy next;

    public AdditionalRatePolicy(PhonePolicy next) {
        this.next = next;
    }
    public Money calculateFee(PhonePolicyManager phonePolicyManager) {
        Money fee = next.calculateFee(phonePolicyManager);
        return calc(fee);
    }
    abstract Money calc(Money fee);
}

class TaxablePolicy extends AdditionalRatePolicy {
    private double taxRatio;

    public TaxablePolicy(PhonePolicy next, double taxRatio) {
        super(next);
        this.taxRatio = taxRatio;
    }

    @Override
    Money calc(Money fee) {
        return fee.plus(fee.times(taxRatio));
    }
}

class RateDiscountablePolicy extends AdditionalRatePolicy {
    private Money discountAmount;

    public RateDiscountablePolicy(PhonePolicy next, Money discountAmount) {
        super(next);
        this.discountAmount = discountAmount;
    }

    @Override
    Money calc(Money fee) {
        return fee.minus(discountAmount);
    }
}

public class TestDriver {
    public static void main(String[] args) {
        PhonePolicyManager phonePolicyManager = new PhonePolicyManager(new RegularPolicy(Money.wons(10), Duration.ofSeconds(10)));
        phonePolicyManager.addPhoneTimesRecord(new PhoneTime(LocalDateTime.of(2020, 1, 1, 10, 0), LocalDateTime.of(2020, 1, 1, 10, 0, 10)));
        phonePolicyManager.addPhoneTimesRecord(new PhoneTime(LocalDateTime.of(2020, 1, 1, 10, 0), LocalDateTime.of(2020, 1, 1, 10, 0, 20)));
        phonePolicyManager.addPhoneTimesRecord(new PhoneTime(LocalDateTime.of(2020, 1, 1, 10, 0), LocalDateTime.of(2020, 1, 1, 10, 0, 30)));
        phonePolicyManager.addPhoneTimesRecord(new PhoneTime(LocalDateTime.of(2020, 1, 1, 22, 0), LocalDateTime.of(2020, 1, 1, 23, 0, 40)));

        System.out.println(phonePolicyManager.calculateFee());

        phonePolicyManager.setPhonePolicy(new NightlyDiscountPolicy(Money.wons(5), Money.wons(10), Duration.ofSeconds(10)));
        System.out.println(phonePolicyManager.calculateFee());

        phonePolicyManager.setPhonePolicy(new TaxablePolicy(new RegularPolicy(Money.wons(10), Duration.ofSeconds(10)), 0.1));
        System.out.println(phonePolicyManager.calculateFee());

        phonePolicyManager.setPhonePolicy(new RateDiscountablePolicy(new TaxablePolicy(new RegularPolicy(Money.wons(10), Duration.ofSeconds(10)), 0.1), Money.wons(5)));
        System.out.println(phonePolicyManager.calculateFee());
    }
}


