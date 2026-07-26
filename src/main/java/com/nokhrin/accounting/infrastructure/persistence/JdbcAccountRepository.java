package com.nokhrin.accounting.infrastructure.persistence;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.account.AccountRepository;
import com.nokhrin.accounting.domain.account.AccountStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class JdbcAccountRepository implements AccountRepository {
    private final DataSource dataSource;

    public JdbcAccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void create(Account account) {
        String createAccountQuery = """
                INSERT INTO accounts (id, balance, status, holder_id, holder_display_name)
                VALUES (?, ?, ?, ?, ?);
                """;

        try (
            Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(createAccountQuery)
        ) {
            preparedStatement.setObject(1, account.id());
            preparedStatement.setObject(2, account.balance());
            preparedStatement.setObject(3, account.status().name());
            preparedStatement.setObject(4, account.holder().id());
            preparedStatement.setObject(5, account.holder().displayName());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to Create account", e);
        }

    }

    @Override
    public void update(Account account) {
        String updateAccountQuery = """
            UPDATE public.accounts
            SET balance=?, status=?
            WHERE id=?;
            """;

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(updateAccountQuery);
        ) {
                preparedStatement.setBigDecimal(1, account.balance());
                preparedStatement.setString(2, account.status().name());
                preparedStatement.setObject(3, account.id());

                int updatedRowsCount = preparedStatement.executeUpdate();
            if (updatedRowsCount == 0) {
                throw new IllegalArgumentException("Update failed, did not find account " + account.id());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Account update failed", e);
        }
    }

    @Override
    public Optional<Account> findById(UUID accountId) {
        String selectByIdQuery = """
                SELECT id, balance, status, holder_id, holder_display_name
                FROM public.accounts where id = ?;
                """;
        try (
            Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(selectByIdQuery)
        ) {
            preparedStatement.setObject(1, accountId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapToDomain(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find account by id: " + accountId, e);
        }
    }

    private Account mapToDomain(ResultSet resultSet) {
        try {
            return new Account(
                    resultSet.getObject("id", UUID.class),
                    resultSet.getBigDecimal("balance"),
                    AccountStatus.valueOf(resultSet.getString("status")),
                    new AccountHolder(
                            resultSet.getObject("holder_id", UUID.class),
                            resultSet.getString("holder_display_name")
                    )
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to map DB response to Account", e);
        }
    }
}
