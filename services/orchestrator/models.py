from pydantic import BaseModel
from typing import List

class Scene(BaseModel):
    scene_id: int
    description: str
    visual_prompt: str
    duration_sec: int
    camera_motion: str
    mood: str
    voice_text: str

class ScenePlan(BaseModel):
    total_duration: int
    style: str
    scenes: List[Scene]