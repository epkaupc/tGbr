import React, { useEffect, useRef, useState } from "react";
import {
  Animated,
  Easing,
  LayoutAnimation,
  View,
  Text,
  FlatList,
  Pressable,
  NativeModules,
  NativeEventEmitter,
  StyleSheet,
  Platform,
  UIManager,
} from "react-native";

type NotificationItem = {
  packageName?: string;
  title?: string;
  text?: string;
  time?: string;
};

const NativeGrabber = NativeModules.NotificationGrabber;

export default function HomeScreen() {
  const [items, setItems] = useState<NotificationItem[]>([]);
  const [status, setStatus] = useState("Aguardando permissão...");
  const [menuOpen, setMenuOpen] = useState(false);
  const menuAnimation = useRef(new Animated.Value(0)).current;
  const badgeAnimation = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    if (Platform.OS === "android" && UIManager.setLayoutAnimationEnabledExperimental) {
      UIManager.setLayoutAnimationEnabledExperimental(true);
    }
  }, []);

  useEffect(() => {
    Animated.timing(menuAnimation, {
      toValue: menuOpen ? 1 : 0,
      duration: 220,
      easing: Easing.out(Easing.cubic),
      useNativeDriver: false,
    }).start();
  }, [menuAnimation, menuOpen]);

  useEffect(() => {
    if (items.length === 0) {
      badgeAnimation.setValue(0);
      return;
    }

    Animated.sequence([
      Animated.timing(badgeAnimation, {
        toValue: 1,
        duration: 160,
        easing: Easing.out(Easing.quad),
        useNativeDriver: true,
      }),
      Animated.spring(badgeAnimation, {
        toValue: 0,
        friction: 5,
        tension: 170,
        useNativeDriver: true,
      }),
    ]).start();
  }, [badgeAnimation, items.length]);

  useEffect(() => {
    console.log("NativeModules keys:", Object.keys(NativeModules));
    console.log("NotificationGrabber:", NativeGrabber);

    if (!NativeGrabber) {
      setStatus("Módulo NotificationGrabber não carregou.");
      return;
    }

    const emitter = new NativeEventEmitter(NativeGrabber);

    const sub = emitter.addListener("notification_received", data => {
      LayoutAnimation.configureNext(LayoutAnimation.Presets.easeInEaseOut);

      const notification: NotificationItem = {
        packageName: data.packageName ?? "App desconhecido",
        title: data.title ?? "Sem título",
        text: data.text ?? "",
        time: new Date().toLocaleTimeString(),
      };

      setItems(prev => [notification, ...prev]);
      setStatus("Capturando notificações...");
    });

    return () => sub.remove();
  }, []);

  function openSettings() {
    if (Platform.OS !== "android") {
      setStatus("Isso só funciona no Android.");
      return;
    }

    if (!NativeGrabber) {
      setStatus("Módulo NotificationGrabber não carregou.");
      console.log("NotificationGrabber está null/undefined");
      return;
    }

    NativeGrabber.openSettings();
  }

  function openOverlaySettings() {
    if (Platform.OS !== "android") {
      setStatus("Isso só funciona no Android.");
      return;
    }

    if (!NativeGrabber) {
      setStatus("Módulo NotificationGrabber não carregou.");
      console.log("NotificationGrabber está null/undefined");
      return;
    }

    NativeGrabber.openOverlaySettings();
  }

  function startBubble() {
    if (Platform.OS !== "android") {
      setStatus("Isso só funciona no Android.");
      return;
    }

    if (!NativeGrabber) {
      setStatus("Módulo NotificationGrabber não carregou.");
      console.log("NotificationGrabber está null/undefined");
      return;
    }

    NativeGrabber.startBubble();
  }

  const menuHeight = menuAnimation.interpolate({
    inputRange: [0, 1],
    outputRange: [0, 252],
  });

  const menuOpacity = menuAnimation.interpolate({
    inputRange: [0, 1],
    outputRange: [0, 1],
  });

  const menuTranslateY = menuAnimation.interpolate({
    inputRange: [0, 1],
    outputRange: [-12, 0],
  });

  const iconRotation = menuAnimation.interpolate({
    inputRange: [0, 1],
    outputRange: ["0deg", "90deg"],
  });

  const badgeScale = badgeAnimation.interpolate({
    inputRange: [0, 0.5, 1],
    outputRange: [1, 1.35, 1],
  });

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Pressable
          style={styles.hamburgerButton}
          onPress={() => setMenuOpen(prev => !prev)}
        >
          <Animated.Text
            style={[styles.hamburgerText, { transform: [{ rotate: iconRotation }] }]}
          >
            ☰
          </Animated.Text>
          {items.length > 0 && (
            <Animated.View
              style={[
                styles.badge,
                {
                  transform: [{ scale: badgeScale }],
                },
              ]}
            >
              <Text style={styles.badgeText}>{items.length}</Text>
            </Animated.View>
          )}
        </Pressable>

        <View>
          <Text style={styles.title}>Notification Grabber</Text>
          <Text style={styles.subtitle}>{status}</Text>
        </View>
      </View>

      <Animated.View
        style={[
          styles.menu,
          {
            height: menuHeight,
            opacity: menuOpacity,
            transform: [{ translateY: menuTranslateY }],
          },
        ]}
        pointerEvents={menuOpen ? "auto" : "none"}
      >
        <View style={styles.menuContent}>
          <Text style={styles.menuTitle}>Menu</Text>

          <Pressable style={styles.button} onPress={openSettings}>
            <Text style={styles.buttonText}>
              Permitir acesso às notificações
            </Text>
          </Pressable>

          <Pressable style={styles.button} onPress={openOverlaySettings}>
            <Text style={styles.buttonText}>
              Permitir aparecer sobre outros apps
            </Text>
          </Pressable>

          <Pressable style={styles.button} onPress={startBubble}>
            <Text style={styles.buttonText}>Mostrar bolha</Text>
          </Pressable>
        </View>
      </Animated.View>

      <FlatList
        data={items}
        keyExtractor={(_, index) => String(index)}
        contentContainerStyle={styles.list}
        ListEmptyComponent={
          <Text style={styles.empty}>
            Nenhuma notificação capturada ainda.
          </Text>
        }
        renderItem={({ item }) => (
          <View style={styles.card}>
            <Text style={styles.package}>{item.packageName}</Text>
            <Text style={styles.cardTitle}>{item.title}</Text>
            <Text style={styles.cardText}>{item.text}</Text>
            <Text style={styles.time}>{item.time}</Text>
          </View>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#0f0f0f",
    paddingTop: 60,
    paddingHorizontal: 18,
  },

  header: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 18,
  },

  hamburgerButton: {
    width: 46,
    height: 46,
    backgroundColor: "#1f1f1f",
    borderRadius: 12,
    alignItems: "center",
    justifyContent: "center",
    marginRight: 12,
    borderWidth: 1,
    borderColor: "#333",
    position: "relative",
  },

  hamburgerText: {
    color: "#fff",
    fontSize: 28,
    fontWeight: "800",
  },

  title: {
    color: "#fff",
    fontSize: 24,
    fontWeight: "800",
  },

  subtitle: {
    color: "#aaa",
    marginTop: 4,
    fontSize: 13,
  },

  menu: {
    backgroundColor: "#181818",
    borderRadius: 14,
    marginBottom: 18,
    borderWidth: 1,
    borderColor: "#333",
    overflow: "hidden",
  },

  menuContent: {
    padding: 14,
  },

  menuTitle: {
    color: "#fff",
    fontSize: 18,
    fontWeight: "800",
    marginBottom: 12,
  },

  button: {
    backgroundColor: "red",
    padding: 14,
    borderRadius: 10,
    alignItems: "center",
    marginBottom: 12,
    
  },

  buttonText: {
    color: "black",
    fontWeight: "700",
    textAlign: "center",
  },

  badge: {
    position: "absolute",
    top: -4,
    right: -4,
    minWidth: 20,
    height: 20,
    paddingHorizontal: 5,
    borderRadius: 10,
    backgroundColor: "#ff4d4d",
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 2,
    borderColor: "#0f0f0f",
  },

  badgeText: {
    color: "#fff",
    fontSize: 11,
    fontWeight: "800",
  },

  list: {
    paddingBottom: 30,
  },

  empty: {
    color: "#777",
    textAlign: "center",
    marginTop: 40,
  },

  card: {
    backgroundColor: "#1f1f1f",
    padding: 14,
    borderRadius: 12,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: "#333",
  },

  package: {
    color: "#60a5fa",
    fontSize: 12,
    marginBottom: 4,
  },

  cardTitle: {
    color: "#fff",
    fontSize: 16,
    fontWeight: "700",
  },

  cardText: {
    color: "#ddd",
    marginTop: 4,
  },

  time: {
    color: "#ff0000",
    marginTop: 8,
    fontSize: 12,
  },
});