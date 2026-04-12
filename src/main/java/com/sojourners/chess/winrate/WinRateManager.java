package com.sojourners.chess.winrate;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;

public class WinRateManager {
    private static WinRateManager instance;

    private Rectangle blackWinBar;
    private Rectangle redWinBar;
    private Label blackPercentLabel;
    private Label redPercentLabel;
    private boolean isReverse = false; // 翻转状态
    private double lastBlackWinRate = 50.0; // 上一次的黑方胜率
    private double lastRedWinRate = 50.0; // 上一次的红方胜率

    private static final double TOTAL_HEIGHT = 650.0; // 总高度

    private WinRateManager() {
    }

    public static WinRateManager getInstance() {
        if (instance == null) {
            instance = new WinRateManager();
        }
        return instance;
    }

    public void initialize(Rectangle blackWinBar, Rectangle redWinBar, Label blackPercentLabel, Label redPercentLabel) {
        this.blackWinBar = blackWinBar;
        this.redWinBar = redWinBar;
        this.blackPercentLabel = blackPercentLabel;
        this.redPercentLabel = redPercentLabel;
        
        // 初始化默认胜率为50%
        updateWinRate(50.0, 50.0);
    }
    
    /**
     * 设置翻转状态
     * @param isReverse 翻转状态
     */
    public void setReverse(boolean isReverse) {
        this.isReverse = isReverse;
        // 翻转后使用上一次的胜率显示，而不是重置为50% 50%
        updateWinRate(lastBlackWinRate, lastRedWinRate);
    }

    /**
     * 更新胜率显示
     * @param blackWinRate 黑方胜率（0-100）
     * @param redWinRate 红方胜率（0-100）
     */
    public void updateWinRate(double blackWinRate, double redWinRate) {
        if (blackWinBar == null || redWinBar == null || blackPercentLabel == null || redPercentLabel == null) {
            return;
        }

        // 保存当前胜率，用于后续使用
        this.lastBlackWinRate = blackWinRate;
        this.lastRedWinRate = redWinRate;

        // 计算柱状图高度
        double blackHeight = (blackWinRate / 100.0) * TOTAL_HEIGHT;
        double redHeight = (redWinRate / 100.0) * TOTAL_HEIGHT;

        // 确保在JavaFX应用线程上更新UI
        Platform.runLater(() -> {
            if (isReverse) {
                // 翻转状态：黑方在下，红方在上
                // 更新红方胜率柱（上半部分）
                redWinBar.setY(0);
                redWinBar.setHeight(redHeight);
                if (redWinRate >= 10 || redWinRate == 100) {
                    if (redWinRate == 100) {
                        redPercentLabel.setText("必胜");
                    } else {
                        redPercentLabel.setText(String.format("%.0f%%", redWinRate));
                    }
                    // 红方百分比显示到红色柱中间
                    redPercentLabel.setLayoutY(redHeight / 2 - 10); // 10是标签高度的一半
                    redPercentLabel.setVisible(true);
                } else {
                    redPercentLabel.setVisible(false);
                }

                // 更新黑方胜率柱（下半部分）
                blackWinBar.setY(redHeight);
                blackWinBar.setHeight(blackHeight);
                if (blackWinRate >= 10 || blackWinRate == 100) {
                    if (blackWinRate == 100) {
                        blackPercentLabel.setText("必胜");
                    } else {
                        blackPercentLabel.setText(String.format("%.0f%%", blackWinRate));
                    }
                    // 黑方百分比显示到黑色柱中间
                    blackPercentLabel.setLayoutY(redHeight + blackHeight / 2 - 10); // 10是标签高度的一半
                    blackPercentLabel.setVisible(true);
                } else {
                    blackPercentLabel.setVisible(false);
                }
            } else {
                // 正常状态：黑方在上，红方在下
                // 更新黑方胜率柱（上半部分）
                blackWinBar.setY(0);
                blackWinBar.setHeight(blackHeight);
                if (blackWinRate >= 10 || blackWinRate == 100) {
                    if (blackWinRate == 100) {
                        blackPercentLabel.setText("必胜");
                    } else {
                        blackPercentLabel.setText(String.format("%.0f%%", blackWinRate));
                    }
                    // 黑方百分比显示到黑色柱中间
                    blackPercentLabel.setLayoutY(blackHeight / 2 - 10); // 10是标签高度的一半
                    blackPercentLabel.setVisible(true);
                } else {
                    blackPercentLabel.setVisible(false);
                }

                // 更新红方胜率柱（下半部分）
                redWinBar.setY(blackHeight);
                redWinBar.setHeight(redHeight);
                if (redWinRate >= 10 || redWinRate == 100) {
                    if (redWinRate == 100) {
                        redPercentLabel.setText("必胜");
                    } else {
                        redPercentLabel.setText(String.format("%.0f%%", redWinRate));
                    }
                    // 红方百分比显示到红色柱中间
                    redPercentLabel.setLayoutY(blackHeight + redHeight / 2 - 10); // 10是标签高度的一半
                    redPercentLabel.setVisible(true);
                } else {
                    redPercentLabel.setVisible(false);
                }
            }
        });
    }

    /**
     * 根据 WDL 数据计算胜率
     * @param winNum 胜利值
     * @param drawNum 和棋值
     * @param loseNum 失败值
     * @param isRedTurn 是否红方回合
     * @return 黑方胜率和红方胜率的数组 [blackWinRate, redWinRate]
     */
    public double[] calculateWinRateFromWDL(int winNum, int drawNum, int loseNum, boolean isRedTurn) {
        double[] winRates = new double[2];
        int totalGames = winNum + drawNum + loseNum;

        if (totalGames == 0) {
            // 如果没有数据，返回上一次的胜率，而不是平局
            winRates[0] = lastBlackWinRate;
            winRates[1] = lastRedWinRate;
        } else {
            // 计算综合胜率：W + D / 2
            double winRate = (double) winNum / totalGames * 100;
            double drawRate = (double) drawNum / totalGames * 100;
            double loseRate = (double) loseNum / totalGames * 100;
            
            double redWinRate, blackWinRate;
            if (isRedTurn) {
                // 红方回合：红方的综合胜率是 W + D/2，黑方的综合胜率是 L + D/2
                redWinRate = winRate + drawRate / 2;
                blackWinRate = loseRate + drawRate / 2;
            } else {
                // 黑方回合：黑方的综合胜率是 W + D/2，红方的综合胜率是 L + D/2
                blackWinRate = winRate + drawRate / 2;
                redWinRate = loseRate + drawRate / 2;
            }
            
            winRates[0] = blackWinRate;
            winRates[1] = redWinRate;
        }

        return winRates;
    }
    
    /**
     * 重置胜率为50:50
     * 用于新建局面、连线断开或重新连线时
     */
    public void resetWinRate() {
        updateWinRate(50.0, 50.0);
    }
}