PIXAR_BASE_PROMPT = '''
Pixar-style 3D animated film scene,
soft global illumination,
cinematic depth of field,
high color vibrancy,
smooth stylized motion,
stable character proportions,
clean geometry,
professional studio quality
'''

CAMERA_MAP = {
    'wide': 'wide establishing shot, slow dolly',
    'medium': 'medium shot, subtle pan',
    'close': 'close-up, gentle drift'
}

def build_pixar_prompt(desc, camera, mood):
    cam = CAMERA_MAP.get(camera, CAMERA_MAP['medium'])
    return f"""{PIXAR_BASE_PROMPT}
Scene: {desc}
Camera: {cam}
Mood: {mood}
"""