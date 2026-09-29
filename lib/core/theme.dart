import 'package:flutter/material.dart';

/// Design tokens and palettes defined in `dual-tone-neumorphic-ui` skill:
/// - 90% neutral surfaces / whitespace (#F6F6F7 canvas, #FFFFFF cards)
/// - 5% orange accent (#F68420) for user intent and primary actions
/// - 3% teal accent (#11CFC9) for system state and healthy/active indicators
/// - 2% supporting navy/gray (#0A0D2F primary text, #223B57 secondary text, #8C929C steel gray)
class NeumorphicColors {
  // Foundation
  static const Color background = Color(0xFFF6F6F7);
  static const Color surface = Color(0xFFFFFFFF);
  static const Color textPrimary = Color(0xFF0A0D2F);
  static const Color textSecondary = Color(0xFF223B57);
  static const Color steelGray = Color(0xFF8C929C);
  static const Color lightGray = Color(0xFFBCBCBF);
  static const Color divider = Color(0xFFE5E7EB);

  // Cool Accents (System state, health, verified, connected)
  static const Color teal = Color(0xFF11CFC9);
  static const Color cyanBlue = Color(0xFF47B3E2);
  static const Color mutedBlue = Color(0xFF496D89);

  // Warm Accents (User intent, primary CTA, actions)
  static const Color orange = Color(0xFFF68420);
  static const Color softOrange = Color(0xFFD68A51);

  // Status highlights
  static const Color error = Color(0xFFE53935);
}

class NeumorphicDepth {
  /// Standard raised soft tactile shadow
  static List<BoxShadow> raised({
    double blur = 12.0,
    double offset = 4.0,
    double opacity = 0.07,
  }) {
    return [
      BoxShadow(
        color: const Color(0xFFFFFFFF),
        offset: Offset(-offset, -offset),
        blurRadius: blur,
        spreadRadius: 1,
      ),
      BoxShadow(
        color: Color.fromRGBO(10, 13, 47, opacity),
        offset: Offset(offset, offset),
        blurRadius: blur,
        spreadRadius: 0,
      ),
    ];
  }

  /// Subtle soft raised shadow for smaller buttons / badges
  static List<BoxShadow> subtleRaised() {
    return raised(blur: 6.0, offset: 2.5, opacity: 0.05);
  }

  /// Inset / pressed visual border & shadow simulation
  static List<BoxShadow> inset() {
    return [
      BoxShadow(
        color: const Color(0x0F0A0D2F),
        offset: const Offset(2, 2),
        blurRadius: 4,
      ),
      const BoxShadow(
        color: Color(0xFFFFFFFF),
        offset: Offset(-2, -2),
        blurRadius: 4,
      ),
    ];
  }
}
