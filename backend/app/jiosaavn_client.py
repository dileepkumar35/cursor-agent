"""JioSaavn API Client - Replicates logic from spoti-down-m4a-opus-v4.2.py"""
import requests
from typing import Optional, Dict


class JioSaavnAPI:
    """Handle JioSaavn API and audio extraction"""
    
    def __init__(self):
        self.base_url = "https://www.jiosaavn.com"
        self.api_url = "https://www.jiosaavn.com/api.php"
    
    def get_song_details(self, song_url: str) -> Optional[Dict]:
        """Get song details from JioSaavn URL"""
        try:
            # Extract song ID from URL - it's the last part after /song/name/
            song_id = song_url.rstrip('/').split('/')[-1]
            print(f"📝 Extracted Song ID: {song_id}")
            
            # Get song details from API
            params = {
                '__call': 'song.getDetails',
                'cc': 'in',
                'pids': song_id,
                '_format': 'json',
                '_marker': '0'
            }
            
            api_response = requests.get(self.api_url, params=params, timeout=10)
            api_response.raise_for_status()
            data = api_response.json()
            
            print(f"🔍 Debug: API Response keys: {list(data.keys()) if isinstance(data, dict) else 'Not a dict'}")
            
            # Try different data structures
            if isinstance(data, dict):
                if song_id in data:
                    print("✅ Found song data by ID")
                    return data[song_id]
                elif 'songs' in data and len(data['songs']) > 0:
                    print("✅ Found song data in 'songs' array")
                    return data['songs'][0]
                else:
                    # JioSaavn sometimes returns with the song ID as key
                    for key in data.keys():
                        if isinstance(data[key], dict):
                            print(f"✅ Found song data with key: {key}")
                            return data[key]
            
            print("⚠️  Could not parse song data")
            return None
            
        except Exception as e:
            print(f"❌ Failed to fetch JioSaavn song details: {e}")
            import traceback
            traceback.print_exc()
            return None
    
    def get_audio_url(self, song_details: Dict, quality: str = '320') -> Optional[str]:
        """Extract audio URL from song details"""
        try:
            # JioSaavn provides URLs for different qualities
            # Check for encrypted media URLs
            if 'encrypted_media_url' in song_details:
                encrypted_url = song_details['encrypted_media_url']
                # Decrypt URL
                audio_url = self.decrypt_url(encrypted_url, quality)
                return audio_url
            elif 'media_preview_url' in song_details:
                return song_details['media_preview_url']
            
            return None
        except Exception as e:
            print(f"❌ Failed to extract audio URL: {e}")
            return None
    
    def decrypt_url(self, encrypted_url: str, quality: str = '320') -> Optional[str]:
        """Decrypt JioSaavn media URL"""
        try:
            # Call JioSaavn API to decrypt
            params = {
                '__call': 'song.generateAuthToken',
                'url': encrypted_url,
                'bitrate': quality,
                '_format': 'json',
                '_marker': '0'
            }
            
            response = requests.get(self.api_url, params=params, timeout=10)
            response.raise_for_status()
            data = response.json()
            
            if 'auth_url' in data:
                return data['auth_url']
            
            return None
        except Exception as e:
            print(f"⚠️  URL decryption failed: {e}")
            return None
