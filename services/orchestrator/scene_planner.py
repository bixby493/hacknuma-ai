from .models import Scene, ScenePlan
from .pixar_prompt_engine import build_pixar_prompt

def generate_scene_plan(script: str, duration: int) -> ScenePlan:
    parts = [s.strip() for s in script.split('.') if s.strip()]
    scenes = []
    per = max(4, duration // max(1,len(parts)))
    for i, p in enumerate(parts):
        scenes.append(Scene(
            scene_id=i+1,
            description=p,
            visual_prompt=build_pixar_prompt(p,'medium','hopeful'),
            duration_sec=per,
            camera_motion='medium',
            mood='hopeful',
            voice_text=p
        ))
    return ScenePlan(total_duration=duration, style='pixar', scenes=scenes)