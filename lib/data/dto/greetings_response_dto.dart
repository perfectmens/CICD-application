import '../../domain/models/greeting.dart';

class GreetingsResponseDto {
  final List<GreetingItemDto> greetings;
  final int count;
  final String timestamp;

  GreetingsResponseDto({
    required this.greetings,
    required this.count,
    required this.timestamp,
  });

  factory GreetingsResponseDto.fromJson(Map<String, dynamic> json) {
    final list = json['greetings'] as List<dynamic>? ?? [];
    return GreetingsResponseDto(
      greetings: list.map((item) => GreetingItemDto.fromJson(item as Map<String, dynamic>)).toList(),
      count: json['count'] as int? ?? list.length,
      timestamp: json['timestamp'] as String? ?? '',
    );
  }

  List<Greeting> toDomain() {
    return greetings.map((item) => item.toDomain()).toList();
  }
}

class GreetingItemDto {
  final int id;
  final String text;
  final String category;
  final String emoji;

  GreetingItemDto({
    required this.id,
    required this.text,
    required this.category,
    required this.emoji,
  });

  factory GreetingItemDto.fromJson(Map<String, dynamic> json) {
    return GreetingItemDto(
      id: json['id'] as int? ?? 0,
      text: json['text'] as String? ?? '',
      category: json['category'] as String? ?? '',
      emoji: json['emoji'] as String? ?? '💬',
    );
  }

  Greeting toDomain() {
    return Greeting(
      id: id,
      text: text,
      category: category,
      emoji: emoji,
    );
  }
}
