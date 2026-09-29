import 'package:flutter/material.dart';
import '../../core/theme.dart';

/// Reusable soft-molded tactile card conforming to dual-tone neumorphic specs
class NeumorphicCard extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry padding;
  final EdgeInsetsGeometry margin;
  final double borderRadius;
  final Color? color;
  final Border? border;
  final VoidCallback? onTap;

  const NeumorphicCard({
    super.key,
    required this.child,
    this.padding = const EdgeInsets.all(20.0),
    this.margin = const EdgeInsets.symmetric(vertical: 8.0, horizontal: 0.0),
    this.borderRadius = 20.0,
    this.color,
    this.border,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    Widget content = Container(
      margin: margin,
      padding: padding,
      decoration: BoxDecoration(
        color: color ?? NeumorphicColors.surface,
        borderRadius: BorderRadius.circular(borderRadius),
        boxShadow: NeumorphicDepth.raised(),
        border: border ?? Border.all(color: Colors.white.withOpacity(0.8), width: 1.5),
      ),
      child: child,
    );

    if (onTap != null) {
      return GestureDetector(
        onTap: onTap,
        child: content,
      );
    }
    return content;
  }
}

/// Tactile Neumorphic Button
/// - Orange = User Intent / Action (Check updates, Download, Install)
/// - Teal = System state action (Sync, Health check)
/// - Neutral = Secondary action
class NeumorphicButton extends StatefulWidget {
  final VoidCallback? onPressed;
  final Widget child;
  final Color? accentColor;
  final bool isPrimary;
  final bool isFullWidth;
  final double borderRadius;
  final EdgeInsetsGeometry padding;

  const NeumorphicButton({
    super.key,
    required this.onPressed,
    required this.child,
    this.accentColor,
    this.isPrimary = true,
    this.isFullWidth = true,
    this.borderRadius = 16.0,
    this.padding = const EdgeInsets.symmetric(vertical: 16.0, horizontal: 24.0),
  });

  @override
  State<NeumorphicButton> createState() => _NeumorphicButtonState();
}

class _NeumorphicButtonState extends State<NeumorphicButton> {
  bool _isPressed = false;

  @override
  Widget build(BuildContext context) {
    final isEnabled = widget.onPressed != null;
    final primaryAccent = widget.accentColor ?? NeumorphicColors.orange;

    return GestureDetector(
      onTapDown: isEnabled ? (_) => setState(() => _isPressed = true) : null,
      onTapUp: isEnabled
          ? (_) {
              setState(() => _isPressed = false);
              widget.onPressed?.call();
            }
          : null,
      onTapCancel: isEnabled ? () => setState(() => _isPressed = false) : null,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 120),
        curve: Curves.easeOutQuad,
        width: widget.isFullWidth ? double.infinity : null,
        padding: widget.padding,
        decoration: BoxDecoration(
          color: !isEnabled
              ? NeumorphicColors.background
              : _isPressed
                  ? const Color(0xFFF0F0F2)
                  : (widget.isPrimary ? primaryAccent : NeumorphicColors.surface),
          borderRadius: BorderRadius.circular(widget.borderRadius),
          boxShadow: !isEnabled || _isPressed
              ? NeumorphicDepth.inset()
              : (widget.isPrimary
                  ? [
                      BoxShadow(
                        color: primaryAccent.withOpacity(0.35),
                        offset: const Offset(0, 6),
                        blurRadius: 16,
                      ),
                      const BoxShadow(
                        color: Colors.white,
                        offset: Offset(-3, -3),
                        blurRadius: 8,
                      ),
                    ]
                  : NeumorphicDepth.raised(blur: 8.0, offset: 3.0)),
          border: Border.all(
            color: widget.isPrimary ? Colors.transparent : Colors.white.withOpacity(0.9),
            width: 1.2,
          ),
        ),
        child: Center(
          child: DefaultTextStyle(
            style: TextStyle(
              color: !isEnabled
                  ? NeumorphicColors.steelGray
                  : (widget.isPrimary ? Colors.white : NeumorphicColors.textPrimary),
              fontWeight: FontWeight.w600,
              fontSize: 15,
              letterSpacing: 0.2,
            ),
            child: widget.child,
          ),
        ),
      ),
    );
  }
}

/// Semantic glowing status dot: Teal = Connected / Online, Orange = Action Required, Gray = Offline
class NeumorphicStatusDot extends StatelessWidget {
  final bool isOnline;
  final double size;

  const NeumorphicStatusDot({
    super.key,
    required this.isOnline,
    this.size = 10.0,
  });

  @override
  Widget build(BuildContext context) {
    final color = isOnline ? NeumorphicColors.teal : NeumorphicColors.steelGray;

    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        color: color,
        shape: BoxShape.circle,
        boxShadow: [
          BoxShadow(
            color: color.withOpacity(isOnline ? 0.6 : 0.2),
            blurRadius: isOnline ? 6.0 : 2.0,
            spreadRadius: isOnline ? 1.5 : 0.0,
          ),
        ],
      ),
    );
  }
}

/// Neumorphic recessed progress bar for streaming download indicator
class NeumorphicProgress extends StatelessWidget {
  final double progress; // 0.0 to 1.0

  const NeumorphicProgress({super.key, required this.progress});

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 12.0,
      width: double.infinity,
      decoration: BoxDecoration(
        color: const Color(0xFFECECEF),
        borderRadius: BorderRadius.circular(6.0),
        boxShadow: const [
          BoxShadow(
            color: Color(0x140A0D2F),
            offset: Offset(1, 1),
            blurRadius: 3,
            spreadRadius: 0,
          ),
          BoxShadow(
            color: Colors.white,
            offset: Offset(-1, -1),
            blurRadius: 2,
            spreadRadius: 0,
          ),
        ],
      ),
      child: LayoutBuilder(
        builder: (context, constraints) {
          final barWidth = constraints.maxWidth * progress.clamp(0.0, 1.0);
          return Align(
            alignment: Alignment.centerLeft,
            child: AnimatedContainer(
              duration: const Duration(milliseconds: 150),
              width: barWidth,
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [NeumorphicColors.softOrange, NeumorphicColors.orange],
                ),
                borderRadius: BorderRadius.circular(6.0),
                boxShadow: [
                  BoxShadow(
                    color: NeumorphicColors.orange.withOpacity(0.35),
                    blurRadius: 4.0,
                    offset: const Offset(0, 1),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}

/// Semantic pill badge (e.g. "v0.0.1", "Online", "SHA-256 Verified")
class NeumorphicBadge extends StatelessWidget {
  final String label;
  final Color color;
  final IconData? icon;

  const NeumorphicBadge({
    super.key,
    required this.label,
    required this.color,
    this.icon,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
      decoration: BoxDecoration(
        color: color.withOpacity(0.08),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color.withOpacity(0.3), width: 1.0),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (icon != null) ...[
            Icon(icon, size: 13, color: color),
            const SizedBox(width: 4),
          ],
          Text(
            label,
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w600,
              color: color,
            ),
          ),
        ],
      ),
    );
  }
}
