package com.nokhrin.bank.operations;

import java.util.UUID;

public sealed interface Operation permits Deposit, Withdraw, Transfer {
}
