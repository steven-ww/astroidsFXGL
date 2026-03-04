import { defineConfig } from "vitest/config";

export default defineConfig({
  root: ".",
  build: {
    outDir: "dist",
    emptyOutDir: true,
  },
  server: {
    open: true,
  },
  test: {
    include: ["tests/**/*.test.ts"],
    environment: "node",
    api: {
      host: "127.0.0.1",
    },
  },
});
