const CONFIG = require('../../../config.js');
const REQUEST = require('../../../utils/request');

Page({
  data: {
    wxAuth: true,
    hasUserInfo: false,
    needUpdateUserInfo: false,

    orders: 0, // 今日订单数
    totalWeight: 0, // 今日交易量
    totalPrice: 0, // 今日交易额
    totalStock: 9999.99, // 剩余库存量



    score_sign_continuous: 0,
    rechargeOpen: false, // 是否开启充值[预存]功能
  },
  onLoad() {
    // 顶部 展示信息
    this.setData({
      version: CONFIG.version
    })
  },
  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      let viewName = wx.getStorageSync('viewName') || this.getTabBar().data.viewName;
      if (viewName == 'master') {
        this.getTabBar().setData({
          selected: 2
        })
      }
      if (viewName == 'developer') {
        this.getTabBar().setData({
          selected: 4
        })
      }
    }

    // 获取订单信息
    this.getSummaryOrderInfo();
    // 获取 清新小句子
    this.getTitle();
    // 每次打开页面,都需要回到顶部。
    if (wx.pageScrollTo) {
      wx.pageScrollTo({
        scrollTop: 0
      })
    }
  },
  // 点击切换试图按钮
  switchView() {
    let viewName = wx.getStorageSync('viewName') || this.getTabBar().data.viewName;;

    if (viewName == 'master') {
      wx.setStorageSync('viewName','developer');
      this.getTabBar().setData({
          selected: 4
        })
    }
    if (viewName == 'developer') {
      wx.setStorageSync('viewName','master');
      this.getTabBar().setData({
        selected: 2
      })
    }
  },
  // 点击切换 清新小句子
  switchContent() {
    this.getTitle();
  },
  // 获取 清新小句子
  getTitle() {
    //a	动画  b	漫画  c	游戏  d	文学  e	原创  f	来自网络
    //g	其他  h	影视  i	诗词  j	网易云  k	哲学 
    wx.request({
      url: 'https://v1.hitokoto.cn?c=b&c=d&c=h&c=j&c=k',
      success: res => {
        this.setData({
          sentenceContent: res.data.hitokoto,
          sentenceFrom: res.data.from
        })
      }
    })
  },
  // 获取订单汇总信息
  getSummaryOrderInfo() {
    let that = this;
    REQUEST.request('manage/getSummaryOrderInfo', 'POST', {
      token: wx.getStorageSync('token'),
    }).then(res => {
      if (res.data.code == 20000) {
        that.setData({
          orders: res.data.data.orders || 0,
          totalPrice: res.data.data.totalPrice || 0,
          totalWeight: res.data.data.totalWeight || 0,
          totalStock: res.data.data.totalStock || 0
        })
      }
    })
  },
})