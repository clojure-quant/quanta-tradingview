(function (global) {

    function DemoBrokerTerminal(host) {
      this._host = host;
  
      this._accounts = [{ id: "DEMO",
                           name: "Demo Account",
                           default: true
                        }];
      this._currentAccountId = "DEMO";
  
      this._orderId = 1;
      this._execId = 1;
  
      this._ordersById = {};
      this._positionsById = {};
      this._executionsBySymbol = {};
    }
  
    // -------------------------
    // Accounts
    // -------------------------
  
    DemoBrokerTerminal.prototype.isTradable = function (symbol) {
        return Promise.resolve(true);
      };

    DemoBrokerTerminal.prototype.accountsMetainfo = function () {
      return Promise.resolve(this._accounts);
    };
  
    DemoBrokerTerminal.prototype.currentAccount = function () {
      return this._currentAccountId;
    };
  
    DemoBrokerTerminal.prototype.setCurrentAccount = function (id) {
      this._currentAccountId = id;
      if (this._host && this._host.currentAccountUpdate) {
        this._host.currentAccountUpdate();
      }
    };
  
    // -------------------------
    // Connection
    // -------------------------
  
    DemoBrokerTerminal.prototype.connectionStatus = function () {
      return 1; // Connected
    };
  
    // -------------------------
    // Orders / Positions
    // -------------------------
  
    DemoBrokerTerminal.prototype.orders = function () {
      return Promise.resolve(Object.values(this._ordersById));
    };
  
    DemoBrokerTerminal.prototype.positions = function () {
      return Promise.resolve(Object.values(this._positionsById));
    };
  
    DemoBrokerTerminal.prototype.executions = function (symbol) {
      return Promise.resolve(this._executionsBySymbol[symbol] || []);
    };
  
    // -------------------------
    // Place Order
    // -------------------------
  
    DemoBrokerTerminal.prototype.placeOrder = function (preOrder) {
      var id = String(this._orderId++);
      var now = Date.now();
  
      var order = {
        id: id,
        symbol: preOrder.symbol,
        side: preOrder.side || "buy",
        type: preOrder.type || "market",
        qty: preOrder.qty || 1,
        status: "working",
        updateTime: now
      };
  
      this._ordersById[id] = order;
  
      if (this._host && this._host.orderUpdate) {
        this._host.orderUpdate(order);
      }
  
      // Auto-fill market orders
      if (order.type === "market") {
        this._fillOrder(order);
      }
  
      return Promise.resolve({ orderId: id });
    };
  
    // -------------------------
    // Cancel Order
    // -------------------------
  
    DemoBrokerTerminal.prototype.cancelOrder = function (orderId) {
      var order = this._ordersById[orderId];
      if (!order) return Promise.resolve();
  
      order.status = "cancelled";
      order.updateTime = Date.now();
  
      if (this._host && this._host.orderUpdate) {
        this._host.orderUpdate(order);
      }
  
      return Promise.resolve();
    };
  
    // -------------------------
    // Symbol Info
    // -------------------------
  
    DemoBrokerTerminal.prototype.symbolInfo = function (symbol) {
      return Promise.resolve({
        symbol: symbol,
        description: symbol,
        minTick: 0.01,
        lotSize: 1,
        currency: "USD"
      });
    };
  
    // -------------------------
    // Internal fill simulation
    // -------------------------
  
    DemoBrokerTerminal.prototype._fillOrder = function (order) {
      var price = 100;
      var now = Date.now();
  
      order.status = "filled";
      order.avgPrice = price;
  
      if (this._host && this._host.orderUpdate) {
        this._host.orderUpdate(order);
      }
  
      var exec = {
        id: String(this._execId++),
        symbol: order.symbol,
        price: price,
        qty: order.qty,
        side: order.side,
        time: now
      };
  
      if (!this._executionsBySymbol[order.symbol]) {
        this._executionsBySymbol[order.symbol] = [];
      }
  
      this._executionsBySymbol[order.symbol].push(exec);
  
      if (this._host && this._host.executionUpdate) {
        this._host.executionUpdate(exec);
      }
  
      var pos = {
        id: order.symbol,
        symbol: order.symbol,
        qty: order.qty,
        side: order.side,
        avgPrice: price,
        updateTime: now
      };
  
      this._positionsById[order.symbol] = pos;
  
      if (this._host && this._host.positionUpdate) {
        this._host.positionUpdate(pos);
      }
    };
  
    // -------------------------
    // Account Manager config
    // -------------------------
  
    DemoBrokerTerminal.prototype.accountManagerInfo = function () {
      return {
        orderColumns: [
          { id: "symbol", label: "Symbol" },
          { id: "side", label: "Side" },
          { id: "qty", label: "Qty" },
          { id: "status", label: "Status" }
        ],
        positionColumns: [
          { id: "symbol", label: "Symbol" },
          { id: "qty", label: "Qty" },
          { id: "avgPrice", label: "Avg Price" }
        ]
      };
    };
  
    // Expose globally
    global.DemoBrokerTerminal = DemoBrokerTerminal;
  
  })(window);
  