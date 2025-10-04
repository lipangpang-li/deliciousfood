package org.example.controller;

import java.util.*;

public class Test {

    private static final double INITIAL_MONEY = 10000.0;
    private static final double TARGET_MONEY = 13000.0;
    private static final int NUMBERS = 36;
    private static final int BET_COUNT = 33;
    private static final double SERVICE_FEE_RATE = 0.02;

    public static void main(String[] args) {
        int numTrials = 10000; // 试验次数
        int wins = 0;
        int losses = 0;
        int totalRounds = 0;

        for (int i = 0; i < numTrials; i++) {
            SimulationResult result = simulateRoulette();
            if (result.outcome.equals("win")) {
                wins++;
            } else {
                losses++;
            }
            totalRounds += result.rounds;
        }

        double winRate = (double) wins / numTrials * 100;
        double lossRate = (double) losses / numTrials * 100;
        double avgRounds = (double) totalRounds / numTrials;

        System.out.println("试验次数: " + numTrials);
        System.out.printf("赚钱次数: %d (%.2f%%)\n", wins, winRate);
        System.out.printf("赔钱次数: %d (%.2f%%)\n", losses, lossRate);
        System.out.printf("平均游戏局数: %.2f\n", avgRounds);
        System.out.println("赚钱概率 " + (winRate > lossRate ? "高于" : "低于") + " 赔钱概率");
    }

    private static SimulationResult simulateRoulette() {
        double currentMoney = INITIAL_MONEY;
        int rounds = 0;
        // 存储最近三局的开奖结果
        Deque<Integer> lastThreeResults = new ArrayDeque<>(3);

        while (currentMoney > 0 && currentMoney < TARGET_MONEY) {
            rounds++;

            // 确定可押注的数字（排除最近三局出现的数字）
            Set<Integer> excludedNumbers = new HashSet<>(lastThreeResults);
            List<Integer> availableNumbers = new ArrayList<>();
            for (int i = 0; i < NUMBERS; i++) {
                if (!excludedNumbers.contains(i)) {
                    availableNumbers.add(i);
                }
            }

            // 随机选择33个数字押注
            Collections.shuffle(availableNumbers);
            List<Integer> betNumbers = availableNumbers.subList(0, Math.min(BET_COUNT, availableNumbers.size()));

            // 计算每份押注金额
            double betPerNumber = currentMoney / BET_COUNT;

            // 轮盘开奖
            int winningNumber = new Random().nextInt(NUMBERS);
            // 更新最近三局结果
            if (lastThreeResults.size() == 3) {
                lastThreeResults.removeFirst();
            }
            lastThreeResults.addLast(winningNumber);

            // 检查是否押中
            if (betNumbers.contains(winningNumber)) {
                // 计算奖金（36倍奖金，扣除服务费）
                double winnings = betPerNumber * 36 * (1 - SERVICE_FEE_RATE);
                // 计算净收益（奖金减去其他32个号码的损失）
                double netGain = winnings - (betPerNumber * (BET_COUNT - 1));
                currentMoney += netGain;
            } else {
                // 未押中，损失全部押注
                currentMoney -= betPerNumber * BET_COUNT;
            }
        }

        return new SimulationResult(
                currentMoney >= TARGET_MONEY ? "win" : "loss",
                rounds
        );
    }

    static class SimulationResult {
        String outcome;
        int rounds;

        SimulationResult(String outcome, int rounds) {
            this.outcome = outcome;
            this.rounds = rounds;
        }
    }
}
