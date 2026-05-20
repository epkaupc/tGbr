import React, { useEffect, useState } from "react";
import {
  View,
  Text,
  FlatList,
  Pressable,
  NativeModules,
  NativeEventEmitter,
  StyleSheet,
  Platform,
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

  useEffect(() => {
    if (!NativeGrabber) {
      setStatus("Módulo nativo ainda não instalado.");
      return;
    }

    const emitter = new NativeEventEmitter(NativeGrabber);

    const sub = emitter.addListener("notification_received", data => {
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
      setStatus("Falta criar o módulo Android.");
      return;
    }

    NativeGrabber.openSettings();
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Notification Grabber</Text>
      <Text style={styles.subtitle}>{status}</Text>

      <Pressable style={styles.button} onPress={openSettings}>
        <Text style={styles.buttonText}>Permitir acesso às notificações</Text>
      </Pressable>

      <Pressable style={styles.button} onPress={() => NativeGrabber.openOverlaySettings()}>
      <Text style={styles.buttonText}>Permitir aparecer sobre outros apps</Text>
      </Pressable>

      <Pressable style={styles.button} onPress={() => NativeGrabber.startBubble()}>
      <Text style={styles.buttonText}>Mostrar bolha</Text>
      </Pressable>


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
  title: {
    color: "#fff",
    fontSize: 28,
    fontWeight: "800",
  },
  subtitle: {
    color: "#aaa",
    marginTop: 6,
    marginBottom: 18,
  },
  button: {
    backgroundColor: "#2563eb",
    padding: 14,
    borderRadius: 10,
    alignItems: "center",
    marginBottom: 16,
  },
  buttonText: {
    color: "#fff",
    fontWeight: "700",
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
    color: "#777",
    marginTop: 8,
    fontSize: 12,
  },
});