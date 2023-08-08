package chap01;

import java.math.BigDecimal;
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

class InvitationCard {
}

class Ticket {
    private String title;
    private Money fee;

    public Ticket(String title, Money fee) {
        this.title = title;
        this.fee = fee;
    }
    public Money getFee() {
        return fee;
    }
}

class Bag {
    private InvitationCard invitationCard;
    private Money money;
    private Ticket ticket;

    public Bag(InvitationCard invitationCard, Money money) {
        this.invitationCard = invitationCard;
        this.money = money;
    }
    public Bag(Money money) {
        this(null, money);
    }
    public void setTicket(TicketOffice ticketOffice) {
        if (invitationCard != null) {
            invitationCard = null;
            this.ticket = ticketOffice.getTicket();
        } else if (money.isLessThan(ticketOffice.getFee())) {
            System.out.println("not enough money");
        } else {
            money = money.minus(ticketOffice.getFee());
            this.ticket = ticketOffice.getTicket();
        }
    }
    public void showState() {
        System.out.println("잔액: " + money);
        System.out.println("티켓: " + ticket);
        System.out.println("초대장: " + invitationCard);
    }
}

class Audience {
    private String id;
    private Bag bag;

    public Audience(String id) {
        this.id = id;
    }
    public void setBag(Bag bag) {
        this.bag = bag;
    }
    public void buyFrom(TicketOffice ticketOffice) {
        bag.setTicket(ticketOffice);
    }
    public void showState() {
        System.out.println("================");
        System.out.println("id: " + id);
        bag.showState();
        System.out.println("================");

    }
}

class TicketOffice {
    private Money money;
    private List<Ticket> tickets = new ArrayList<>();

    public TicketOffice(Money money, Ticket ... t) {
        this.money = money;
        tickets.addAll(Arrays.asList(t));
    }

    public Ticket getTicket() {
        if (tickets.isEmpty()) {
            throw new IndexOutOfBoundsException();
        }
        money.plus(getFee());
        return tickets.remove(0); 
    }

    public Money getFee() {
        if (tickets.isEmpty()) {
            throw new IndexOutOfBoundsException();
        }
        return tickets.get(0).getFee();
    }
}

class TicketSeller {
    private TicketOffice ticketOffice;

    public TicketSeller(TicketOffice ticketOffice) {
        this.ticketOffice = ticketOffice;
    }

    public void sellTo(Audience audience) {
        audience.buyFrom(ticketOffice);
    }
}

class Theater {
    private TicketSeller ticketSeller;

    void enter(Audience audience) {
        ticketSeller.sellTo(audience);
    }
}

public class TestDriver {
    public static void main(String[] args) {
        Audience audience1 = new Audience("Lee");
        Audience audience2 = new Audience("Kim");
        Audience audience3 = new Audience("Park");

        audience1.setBag(new Bag(new InvitationCard(), Money.wons(10000)));
        audience2.setBag(new Bag(Money.wons(10000)));
        audience3.setBag(new Bag(Money.wons(5000)));

        audience1.showState();
        audience2.showState();
        audience3.showState();

        Ticket ticket1 = new Ticket("콘서트", Money.wons(10000));
        Ticket ticket2 = new Ticket("콘서트", Money.wons(10000));
        Ticket ticket3 = new Ticket("콘서트", Money.wons(10000));

        TicketOffice ticketOffice = new TicketOffice(Money.ZERO, ticket1, ticket2, ticket3);
        TicketSeller ticketSeller = new TicketSeller(ticketOffice);
        ticketSeller.sellTo(audience1);
        ticketSeller.sellTo(audience2);
        ticketSeller.sellTo(audience3);

        audience1.showState();
        audience2.showState();
        audience3.showState();
    }
}