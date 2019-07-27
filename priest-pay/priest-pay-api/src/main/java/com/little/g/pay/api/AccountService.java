package com.little.g.pay.api;

import com.little.g.common.dto.Page;
import com.little.g.pay.dto.Account;
import com.little.g.pay.dto.FrozenRecordDTO;
import com.little.g.pay.dto.TransactionRecordDTO;
import com.little.g.pay.dto.UserAccountDTO;
import com.little.g.pay.enums.BusinessType;

import java.util.List;

/**
 * Created by zhaoyao on 16/5/6.
 */
public interface AccountService {
    /**
     * 创建账户
     * @param uid 用户ID
     */
    void createUserAccount(long uid);

    /**
     * 获取账户信息
     * @param account
     * @return
     */
    UserAccountDTO get(Account account);

    /**
     *  统计一段时间账户交易总额
     * @param account
     * @param timeStart
     * @param timeEnd
     * @return
     */
    long getBalance(Account account, long timeStart, long timeEnd);

    /**
     * 拉取账户交易记录
     * @param account
     * @param startTime
     * @param endTime
     * @param limit
     * @return
     */
    List<TransactionRecordDTO> findTransactions(Account account, Long startTime, Long endTime, Integer limit);

    /**
     * 转账
     * @param fromUid
     * @param toUid
     * @param amount
     * @param transNum
     * @param btype
     * @param desc
     * @return
     */
    List<TransactionRecordDTO> transfer(long fromUid, long toUid, long amount, String transNum, BusinessType btype, String desc);

    /**
     * 冻结
     * @param uid
     * @param amount
     * @param transNum
     * @param btype
     * @param desc
     * @return
     */
    List<TransactionRecordDTO> froze(long uid, long amount, String transNum, BusinessType btype, String desc);

    /**
     * 解冻
     * @param srcTransNum
     * @param amount
     * @param toUid
     * @param transNum
     * @param btype
     * @param desc
     * @return
     */
    List<TransactionRecordDTO> unfroze(String srcTransNum, long amount, long toUid, String transNum, BusinessType btype, String desc);

    /**
     *  分页查询
     * @param uid
     * @param page
     * @param pageSize
     * @return
     */
    Page<FrozenRecordDTO> listFrozenRecords(long uid, int page, int pageSize);

    /**
     *  创建账户
     * @param account
     */
    void createAccount(Account account);

    /**
     * 转账
     * @param from
     * @param to
     * @param amount
     * @param transNum
     * @param btype
     * @param desc
     * @return
     */
    List<TransactionRecordDTO> transfer(Account from, Account to, long amount, String transNum, BusinessType btype, String desc);

    /**
     * 分页查询冻结订单
     * @param account
     * @param page
     * @param pageSize
     * @return
     */
    Page<FrozenRecordDTO> listFrozenRecords(Account account, int page, int pageSize);
}
