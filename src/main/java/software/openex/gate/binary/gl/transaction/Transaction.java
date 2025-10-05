/*
 * ISC License
 *
 * Copyright (c) 2025, Alireza Pourtaghi <lirezap@protonmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package software.openex.gate.binary.gl.transaction;

import software.openex.gate.binary.gl.wallet.Wallet;

import static java.lang.Math.addExact;
import static software.openex.gate.binary.BinaryRepresentable.representationSize;

/**
 * @author Alireza Pourtaghi
 */
public final class Transaction {
    private final int ledger;
    private final long sourceAccount;
    private final int sourceWallet;
    private final long destinationAccount;
    private final int destinationWallet;
    private final String id;
    private final String currency;
    private final long amount;
    private final long maxOverdraftAmount;
    private final String metadata;
    private long sourceWalletNewBalance;
    private long destinationWalletNewBalance;
    private long ts;
    private boolean _failed;
    private String _failReason;
    private Wallet _sourceWallet;
    private Wallet _destinationWallet;

    public Transaction(final int ledger, final String id) {
        this(ledger, 0, 0, 0, 0, id, "", 0, 0, "", 0, 0, 0);
    }

    public Transaction(final int ledger, final long sourceAccount, final int sourceWallet,
                       final long destinationAccount, final int destinationWallet, final String id,
                       final String currency, final long amount, final long maxOverdraftAmount, final String metadata) {

        this(ledger, sourceAccount, sourceWallet, destinationAccount, destinationWallet, id, currency, amount,
                maxOverdraftAmount, metadata, 0, 0, 0);
    }

    public Transaction(final int ledger, final long sourceAccount, final int sourceWallet,
                       final long destinationAccount, final int destinationWallet, final String id,
                       final String currency, final long amount, final long maxOverdraftAmount, final String metadata,
                       final long sourceWalletNewBalance, final long destinationWalletNewBalance, final long ts) {

        this.ledger = ledger;
        this.sourceAccount = sourceAccount;
        this.sourceWallet = sourceWallet;
        this.destinationAccount = destinationAccount;
        this.destinationWallet = destinationWallet;
        this.id = id == null ? "" : id;
        this.currency = currency == null ? "" : currency;
        this.amount = amount;
        this.maxOverdraftAmount = maxOverdraftAmount;
        this.metadata = metadata == null ? "" : metadata;
        this.sourceWalletNewBalance = sourceWalletNewBalance;
        this.destinationWalletNewBalance = destinationWalletNewBalance;
        this.ts = ts;
    }

    public int size() {
        return addExact(28,
                addExact(representationSize(id),
                        addExact(representationSize(currency),
                                addExact(16,
                                        addExact(representationSize(metadata), 24)))));
    }

    public int getLedger() {
        return ledger;
    }

    public long getSourceAccount() {
        return sourceAccount;
    }

    public int getSourceWallet() {
        return sourceWallet;
    }

    public long getDestinationAccount() {
        return destinationAccount;
    }

    public int getDestinationWallet() {
        return destinationWallet;
    }

    public String getId() {
        return id;
    }

    public String getCurrency() {
        return currency;
    }

    public long getAmount() {
        return amount;
    }

    public long getMaxOverdraftAmount() {
        return maxOverdraftAmount;
    }

    public String getMetadata() {
        return metadata;
    }

    public long getSourceWalletNewBalance() {
        return sourceWalletNewBalance;
    }

    public long getDestinationWalletNewBalance() {
        return destinationWalletNewBalance;
    }

    public long getTs() {
        return ts;
    }

    public boolean is_failed() {
        return _failed;
    }

    public String get_failReason() {
        return _failReason;
    }

    public Wallet get_sourceWallet() {
        return _sourceWallet;
    }

    public Wallet get_destinationWallet() {
        return _destinationWallet;
    }

    public void setSourceWalletNewBalance(final long sourceWalletNewBalance) {
        this.sourceWalletNewBalance = sourceWalletNewBalance;
    }

    public void setDestinationWalletNewBalance(final long destinationWalletNewBalance) {
        this.destinationWalletNewBalance = destinationWalletNewBalance;
    }

    public void setTs(final long ts) {
        this.ts = ts;
    }

    public void set_failed(final boolean _failed) {
        this._failed = _failed;
    }

    public void set_failReason(final String _failReason) {
        this._failReason = _failReason;
    }

    public void set_sourceWallet(final Wallet _sourceWallet) {
        this._sourceWallet = _sourceWallet;
    }

    public void set_destinationWallet(final Wallet _destinationWallet) {
        this._destinationWallet = _destinationWallet;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction transaction)) return false;

        return getLedger() == transaction.getLedger() && getId().equals(transaction.getId());
    }

    @Override
    public int hashCode() {
        int result = getLedger();
        result = 31 * result + getId().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "ledger=" + ledger +
                ", sourceAccount=" + sourceAccount +
                ", sourceWallet=" + sourceWallet +
                ", destinationAccount=" + destinationAccount +
                ", destinationWallet=" + destinationWallet +
                ", id='" + id + '\'' +
                ", currency='" + currency + '\'' +
                ", amount=" + amount +
                ", maxOverdraftAmount=" + maxOverdraftAmount +
                ", metadata='" + metadata + '\'' +
                ", sourceWalletNewBalance=" + sourceWalletNewBalance +
                ", destinationWalletNewBalance=" + destinationWalletNewBalance +
                ", ts=" + ts +
                ", _failed=" + _failed +
                ", _failReason='" + _failReason + '\'' +
                ", _sourceWallet=" + _sourceWallet +
                ", _destinationWallet=" + _destinationWallet +
                '}';
    }
}
