"""Spotify API Client - Replicates logic from spoti-down-m4a-opus-v4.2.py"""
import base64
import requests
from typing import Optional, Dict


class SpotifyAPI:
    """Handle Spotify API authentication and data fetching"""
    
    def __init__(self, client_id: str, client_secret: str):
        self.client_id = client_id
        self.client_secret = client_secret
        self.access_token = None
        
    def get_access_token(self) -> bool:
        """Get Spotify API access token"""
        auth_url = "https://accounts.spotify.com/api/token"
        auth_header = base64.b64encode(
            f"{self.client_id}:{self.client_secret}".encode()
        ).decode()
        
        headers = {
            "Authorization": f"Basic {auth_header}",
            "Content-Type": "application/x-www-form-urlencoded"
        }
        
        data = {"grant_type": "client_credentials"}
        
        try:
            response = requests.post(auth_url, headers=headers, data=data, timeout=10)
            response.raise_for_status()
            self.access_token = response.json()["access_token"]
            return True
        except Exception as e:
            print(f"❌ Failed to authenticate with Spotify: {e}")
            return False
    
    def get_track_info(self, track_url: str) -> Optional[Dict]:
        """Get track information from Spotify URL"""
        if not self.access_token:
            if not self.get_access_token():
                return None
        
        # Extract track ID from URL
        track_id = track_url.split("/track/")[-1].split("?")[0]
        
        api_url = f"https://api.spotify.com/v1/tracks/{track_id}"
        headers = {"Authorization": f"Bearer {self.access_token}"}
        
        try:
            response = requests.get(api_url, headers=headers, timeout=10)
            response.raise_for_status()
            return response.json()
        except Exception as e:
            print(f"❌ Failed to fetch track info: {e}")
            return None
