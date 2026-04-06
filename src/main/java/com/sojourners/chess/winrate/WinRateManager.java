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
        // 翻转后重新更新胜率显示
        updateWinRate(50.0, 50.0);
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
                if (redWinRate > 0 || redWinRate == 100) {
                    redPercentLabel.setText(String.format("%.0f%%", redWinRate));
                    // 红方百分比显示到红色柱中间
                    redPercentLabel.setLayoutY(redHeight / 2 - 10); // 10是标签高度的一半
                    redPercentLabel.setVisible(true);
                } else {
                    redPercentLabel.setVisible(false);
                }

                // 更新黑方胜率柱（下半部分）
                blackWinBar.setY(redHeight);
                blackWinBar.setHeight(blackHeight);
                if (blackWinRate > 0 || blackWinRate == 100) {
                    blackPercentLabel.setText(String.format("%.0f%%", blackWinRate));
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
                if (blackWinRate > 0 || blackWinRate == 100) {
                    blackPercentLabel.setText(String.format("%.0f%%", blackWinRate));
                    // 黑方百分比显示到黑色柱中间
                    blackPercentLabel.setLayoutY(blackHeight / 2 - 10); // 10是标签高度的一半
                    blackPercentLabel.setVisible(true);
                } else {
                    blackPercentLabel.setVisible(false);
                }

                // 更新红方胜率柱（下半部分）
                redWinBar.setY(blackHeight);
                redWinBar.setHeight(redHeight);
                if (redWinRate > 0 || redWinRate == 100) {
                    redPercentLabel.setText(String.format("%.0f%%", redWinRate));
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
     * 根据引擎分数计算胜率
     * @param score 引擎评估分数
     * @return 黑方胜率和红方胜率的数组 [blackWinRate, redWinRate]
     */
    public double[] calculateWinRate(int score) {
        double[] winRates = new double[2];

        // 简单的胜率计算逻辑，基于分数
        // 分数为正，红方优势；分数为负，黑方优势
        double ratio;
        if (score > 0) {
            // 红方优势
            ratio = Math.min(score / 1000.0, 1.0);
            winRates[1] = 50 + ratio * 50; // 红方胜率
            winRates[0] = 100 - winRates[1]; // 黑方胜率
        } else if (score < 0) {
            // 黑方优势
            ratio = Math.min(Math.abs(score) / 1000.0, 1.0);
            winRates[0] = 50 + ratio * 50; // 黑方胜率
            winRates[1] = 100 - winRates[0]; // 红方胜率
        } else {
            // 平局
            winRates[0] = 50.0;
            winRates[1] = 50.0;
        }

        return winRates;
    }
}