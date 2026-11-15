import os
import threading

_srv = None


def start(data_dir, port=8042):
    """يشغّل خادم Z-Wifi داخل التطبيق (يُستدعى من MainActivity)."""
    global _srv
    if _srv:
        return 'already'
    os.environ['ZWIFI_DATA'] = data_dir
    import zwifi
    zwifi.DEVF = os.path.join(data_dir, 'devices.json')
    _srv = zwifi.serve(port)
    threading.Thread(target=_srv.serve_forever, daemon=True).start()
    return 'ok'
