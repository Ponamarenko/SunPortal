#!/usr/bin/env python3
"""
Тестовое соединение с Deye Gateway Server
Проверяет, что сервер доступен и возвращает корректные данные
"""

import requests
import json
import time
from datetime import datetime

BASE_URL = "http://192.168.25.117:8081"  # IP нашего интеграционного сервера

def test_server_connection():
    """Тестирование соединения с сервером"""
    print("🧪 Тестирование соединения с Deye Gateway Server")
    print("=" * 50)
    
    results = {}
    
    # Тест 1: Базовый эндпоинт
    print("\n1. Тест базового эндпоинта...")
    try:
        response = requests.get(f"{BASE_URL}/", timeout=5)
        if response.status_code == 200:
            results["base"] = {"status": "✅ OK", "response": response.json()}
            print("   ✅ Базовый эндпоинт работает")
        else:
            results["base"] = {"status": "❌ ERROR", "code": response.status_code}
            print(f"   ❌ Ошибка: {response.status_code}")
    except Exception as e:
        results["base"] = {"status": "❌ ERROR", "error": str(e)}
        print(f"   ❌ Ошибка подключения: {e}")
    
    # Тест 2: Статус системы
    print("\n2. Тест статуса системы...")
    try:
        response = requests.get(f"{BASE_URL}/status", timeout=5)
        if response.status_code == 200:
            data = response.json()
            results["status"] = {"status": "✅ OK", "data": data}
            print(f"   ✅ Статус: {data.get('status', 'Unknown')}")
            print(f"   ✅ Версия: {data.get('version', 'Unknown')}")
            print(f"   ✅ Инвертор подключен: {data.get('inverter_connected', False)}")
        else:
            results["status"] = {"status": "❌ ERROR", "code": response.status_code}
            print(f"   ❌ Ошибка: {response.status_code}")
    except Exception as e:
        results["status"] = {"status": "❌ ERROR", "error": str(e)}
        print(f"   ❌ Ошибка подключения: {e}")
    
    # Тест 3: Live данные
    print("\n3. Тест live данных...")
    try:
        response = requests.get(f"{BASE_URL}/live", timeout=5)
        if response.status_code == 200:
            data = response.json()
            results["live"] = {"status": "✅ OK", "data_keys": list(data.keys())}
            print("   ✅ Live данные получены")
            print(f"   📊 Данных: {len(data)}")
            
            # Показать ключевые параметры
            key_params = ["battery_soc", "pv1_power", "pv2_power", "pv3_power", "grid_voltage"]
            for param in key_params:
                if param in data:
                    value = data[param]
                    unit = get_unit_for_param(param)
                    print(f"   📊 {param}: {value} {unit}")
        else:
            results["live"] = {"status": "❌ ERROR", "code": response.status_code}
            print(f"   ❌ Ошибка: {response.status_code}")
    except Exception as e:
        results["live"] = {"status": "❌ ERROR", "error": str(e)}
        print(f"   ❌ Ошибка подключения: {e}")
    
    # Тест 4: Health check
    print("\n4. Тест health check...")
    try:
        response = requests.get(f"{BASE_URL}/deye/health", timeout=5)
        if response.status_code == 200:
            data = response.json()
            results["health"] = {"status": "✅ OK", "data": data}
            print("   ✅ Health check пройден")
            print(f"   📊 Статус подключения: {data.get('connection_status', 'Unknown')}")
            print(f"   📊 Доступных регистров: {data.get('available_registers', 0)}")
        else:
            results["health"] = {"status": "❌ ERROR", "code": response.status_code}
            print(f"   ❌ Ошибка: {response.status_code}")
    except Exception as e:
        results["health"] = {"status": "❌ ERROR", "error": str(e)}
        print(f"   ❌ Ошибка подключения: {e}")
    
    # Тест 5: Список регистров
    print("\n5. Тест списка регистров...")
    try:
        response = requests.get(f"{BASE_URL}/registers", timeout=5)
        if response.status_code == 200:
            data = response.json()
            registers = data.get("registers", [])
            results["registers"] = {"status": "✅ OK", "count": len(registers)}
            print(f"   ✅ Найдено регистров: {len(registers)}")
            
            # Показать первые 5 регистров
            for i, reg in enumerate(registers[:5]):
                print(f"   📊 {reg['address']}: {reg['name']} ({reg['unit']})")
            if len(registers) > 5:
                print(f"   ... и еще {len(registers) - 5} регистров")
        else:
            results["registers"] = {"status": "❌ ERROR", "code": response.status_code}
            print(f"   ❌ Ошибка: {response.status_code}")
    except Exception as e:
        results["registers"] = {"status": "❌ ERROR", "error": str(e)}
        print(f"   ❌ Ошибка подключения: {e}")
    
    # Итоги
    print("\n" + "=" * 50)
    print("📊 Итоги тестирования:")
    success_count = sum(1 for r in results.values() if r.get("status") == "✅ OK")
    total_count = len(results)
    print(f"✅ Успешно: {success_count}/{total_count}")
    
    if success_count == total_count:
        print("🎉 Все тесты пройдены успешно!")
        print("✅ Сервер готов для мобильного приложения")
    else:
        print("⚠️  Некоторые тесты не пройдены")
        print("❌ Требуется проверка сервера")
    
    return results

def get_unit_for_param(param: str) -> str:
    """Получить единицу измерения для параметра"""
    units = {
        "battery_soc": "%",
        "battery_voltage": "V",
        "battery_temp": "°C",
        "battery_power": "W",
        "battery_current": "A",
        "pv1_power": "W",
        "pv2_power": "W",
        "pv3_power": "W",
        "grid_voltage": "V",
        "grid_frequency": "Hz",
        "grid_power": "W",
        "load_power": "W",
        "daily_pv_production": "kWh",
        "total_pv_production": "kWh",
        "daily_buy_energy": "kWh",
        "daily_sell_energy": "kWh",
    }
    return units.get(param, "")

if __name__ == "__main__":
    # Запуск тестов
    test_results = test_server_connection()
    
    # Сохранение отчета
    test_report = {
        "timestamp": datetime.now().isoformat(),
        "server_url": BASE_URL,
        "tests": test_results,
        "summary": {
            "success_count": sum(1 for r in test_results.values() if r.get("status") == "✅ OK"),
            "total_count": len(test_results)
        }
    }
    
    # Сохранение отчета
    with open("server_test_report.json", "w", encoding="utf-8") as f:
        json.dump(test_report, f, indent=2, ensure_ascii=False)
    
    print(f"\n📄 Отчет сохранен в: server_test_report.json")
    
    # Рекомендации
    print("\n💡 Рекомендации:")
    if test_results["status"]["status"] == "✅ OK":
        print("✅ Сервер работает корректно")
        print("✅ Готов для интеграции с мобильным приложением")
        print("✅ Все эндпоинты доступны")
    else:
        print("❌ Требуется проверка конфигурации сервера")
        print("❌ Проверьте логи сервера")