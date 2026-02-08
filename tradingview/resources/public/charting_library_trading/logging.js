window.attachBrokerLogging = function (broker, name) {
    name = name || "Broker";
  
    if (!broker) {
      console.error("attachBrokerLogging: broker is undefined");
      return broker;
    }
  
    var proto = Object.getPrototypeOf(broker);
  
    Object.getOwnPropertyNames(proto).forEach(function (key) {
      if (key === "constructor") return;
  
      var original = broker[key];
  
      if (typeof original !== "function") return;
  
      broker[key] = function () {
        var args = Array.prototype.slice.call(arguments);
  
        console.log("📞 " + name + "." + key + " called with:", args);
  
        try {
          var result = original.apply(this, args);
  
          // Promise handling
          if (result && typeof result.then === "function") {
            return result.then(function (value) {
              console.log("✅ " + name + "." + key + " resolved:", value);
              return value;
            }).catch(function (err) {
              console.error("❌ " + name + "." + key + " rejected:", err);
              throw err;
            });
          }
  
          console.log("✅ " + name + "." + key + " returned:", result);
          return result;
  
        } catch (err) {
          console.error("❌ " + name + "." + key + " threw:", err);
          throw err;
        }
      };
    });
  
    console.log("🔍 Logging attached to broker:", name);
  
    return broker;
  };
  