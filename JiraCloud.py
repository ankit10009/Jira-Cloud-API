import requests
import base64
import json
from requests.auth import HTTPBasicAuth
import time
import os
import urllib3

class JiraAPI:
    def __init__(self, base_url, username, api_token, proxy_config=None):
        """
        Initialize Jira API client
        
        Args:
            base_url (str): Your Jira instance URL (e.g., 'https://yourcompany.atlassian.net')
            username (str): Your Jira username/email
            api_token (str): Your Jira API token
            proxy_config (dict): Proxy configuration if needed
        """
        self.base_url = base_url.rstrip('/')
        self.username = username
        self.api_token = api_token
        self.session = requests.Session()
        
        # Configure proxy if provided
        if proxy_config:
            self.session.proxies.update(proxy_config)
            print(f"Using proxy configuration: {proxy_config}")
        
        # Check for system proxy settings
        self._configure_proxy_from_environment()
        
        # Set up authentication
        self.session.auth = HTTPBasicAuth(username, api_token)
        
        # Set default headers to match Postman behavior
        self.session.headers.update({
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'X-Atlassian-Token': 'no-check',
            'User-Agent': 'Python-Jira-Client/1.0',
            'Connection': 'keep-alive',
            'Accept-Encoding': 'gzip, deflate, br',
            'Cache-Control': 'no-cache'
        })
        
        # Configure timeouts and retries
        self.timeout = 30
        self.max_retries = 3
        
        # Enable connection pooling and keep-alive like Postman
        adapter = requests.adapters.HTTPAdapter(
            pool_connections=10,
            pool_maxsize=10,
            max_retries=0  # We handle retries manually
        )
        self.session.mount('http://', adapter)
        self.session.mount('https://', adapter)
    
    def _configure_proxy_from_environment(self):
        """
        Configure proxy from environment variables
        """
        http_proxy = os.getenv('HTTP_PROXY') or os.getenv('http_proxy')
        https_proxy = os.getenv('HTTPS_PROXY') or os.getenv('https_proxy')
        no_proxy = os.getenv('NO_PROXY') or os.getenv('no_proxy')
        
        if http_proxy or https_proxy:
            proxy_dict = {}
            if http_proxy:
                proxy_dict['http'] = http_proxy
            if https_proxy:
                proxy_dict['https'] = https_proxy
                
            self.session.proxies.update(proxy_dict)
            print(f"Detected system proxy settings: {proxy_dict}")
            
            if no_proxy:
                print(f"No proxy for: {no_proxy}")
        
        # Disable SSL warnings for corporate proxies (if needed)
        # Uncomment the next line if you have SSL certificate issues with corporate proxy
        # urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
    
    def make_request(self, endpoint, method='GET', params=None, data=None):
        """
        Make a request to Jira API with error handling and retries
        
        Args:
            endpoint (str): API endpoint (e.g., '/rest/api/3/field')
            method (str): HTTP method
            params (dict): Query parameters
            data (dict): Request body data
            
        Returns:
            dict: JSON response or None if failed
        """
        url = f"{self.base_url}{endpoint}"
        
        for attempt in range(self.max_retries):
            try:
                print(f"Attempt {attempt + 1}/{self.max_retries}: Making {method} request to: {url}")
                print(f"Headers: {dict(self.session.headers)}")
                
                response = self.session.request(
                    method=method,
                    url=url,
                    params=params,
                    json=data,
                    timeout=self.timeout,
                    verify=True,  # Set to False if corporate proxy has SSL issues
                    allow_redirects=True
                )
                
                print(f"Response status code: {response.status_code}")
                
                # Check if request was successful
                response.raise_for_status()
                
                return response.json()
                
            except requests.exceptions.Timeout:
                print(f"Timeout error on attempt {attempt + 1}/{self.max_retries}")
                if attempt < self.max_retries - 1:
                    wait_time = 2 ** attempt
                    print(f"Waiting {wait_time} seconds before retry...")
                    time.sleep(wait_time)
                else:
                    print("Max retries reached. Request failed due to timeout.")
                    return None
                    
            except requests.exceptions.ConnectionError as e:
                print(f"Connection error on attempt {attempt + 1}/{self.max_retries}: {e}")
                print("This could be due to:")
                print("- Incorrect Jira URL")
                print("- Network connectivity issues")
                print("- Jira instance being down")
                print("- Firewall blocking the connection")
                
                if attempt < self.max_retries - 1:
                    wait_time = 2 ** attempt
                    print(f"Waiting {wait_time} seconds before retry...")
                    time.sleep(wait_time)
                else:
                    print("Max retries reached. Connection failed.")
                    return None
                    
            except requests.exceptions.HTTPError as e:
                print(f"HTTP error: {e}")
                print(f"Response status: {response.status_code}")
                print(f"Response text: {response.text}")
                return None
                
            except Exception as e:
                print(f"Unexpected error: {e}")
                return None
    
    def get_fields(self):
        """
        Fetch all fields from Jira
        
        Returns:
            list: List of field objects
        """
        return self.make_request('/rest/api/3/field')
    
    def get_custom_fields(self):
        """
        Fetch only custom fields from Jira
        
        Returns:
            list: List of custom field objects
        """
        fields = self.get_fields()
        if fields:
            return [field for field in fields if field.get('custom', False)]
        return []
    
    def test_connection(self):
        """
        Test the connection to Jira API
        First make a simple request to establish session like Postman does
        """
        # First, try to get server info (this often generates session cookies)
        print("Step 1: Getting server info to establish session...")
        server_info = self.make_request('/rest/api/3/serverInfo')
        
        if server_info:
            print(f"Server: {server_info.get('serverTitle', 'Unknown')} - {server_info.get('version', 'Unknown')}")
        
        # Then test user authentication
        print("Step 2: Testing user authentication...")
        result = self.make_request('/rest/api/3/myself')
        if result:
            print(f"✅ Connection successful! Logged in as: {result.get('displayName', 'Unknown')}")
            print(f"Email: {result.get('emailAddress', 'Unknown')}")
            return True
        else:
            print("❌ Connection failed!")
            return False

def get_proxy_configuration():
    """
    Get proxy configuration from user
    """
    print("\n🔧 PROXY CONFIGURATION")
    print("=" * 40)
    
    use_proxy = input("Are you behind a corporate proxy? (y/n): ").lower().strip()
    
    if use_proxy in ['y', 'yes']:
        print("\nProxy configuration options:")
        print("1. Auto-detect from environment variables")
        print("2. Manual configuration")
        print("3. Skip proxy (direct connection)")
        
        choice = input("Choose option (1/2/3): ").strip()
        
        if choice == '1':
            # Check environment variables
            http_proxy = os.getenv('HTTP_PROXY') or os.getenv('http_proxy')
            https_proxy = os.getenv('HTTPS_PROXY') or os.getenv('https_proxy')
            
            if http_proxy or https_proxy:
                print(f"Found system proxy settings:")
                if http_proxy:
                    print(f"  HTTP: {http_proxy}")
                if https_proxy:
                    print(f"  HTTPS: {https_proxy}")
                return None  # Will be auto-configured
            else:
                print("No system proxy found. Please configure manually.")
                choice = '2'
        
        if choice == '2':
            proxy_host = input("Enter proxy host (e.g., proxy.company.com): ").strip()
            proxy_port = input("Enter proxy port (e.g., 8080): ").strip()
            
            proxy_user = input("Proxy username (press Enter if none): ").strip()
            proxy_pass = input("Proxy password (press Enter if none): ").strip()
            
            if proxy_user and proxy_pass:
                proxy_url = f"http://{proxy_user}:{proxy_pass}@{proxy_host}:{proxy_port}"
            else:
                proxy_url = f"http://{proxy_host}:{proxy_port}"
            
            return {
                'http': proxy_url,
                'https': proxy_url
            }
    
    return None
def test_basic_connectivity(proxy_config=None):
    print("="*60)
    print("JIRA CONNECTION TROUBLESHOOTING")
    print("="*60)
    
    # Test URL format
    test_url = input("Enter your Jira URL (e.g., https://yourcompany.atlassian.net): ").strip()
    
    if not test_url.startswith('http'):
        print("❌ URL should start with https://")
        return False
    
    if '.atlassian.net' not in test_url and 'localhost' not in test_url:
        print("⚠️  Are you sure this is a correct Jira URL?")
    
def test_basic_connectivity(proxy_config=None):
    """
    Test basic connectivity and URL format
    """
    print("="*60)
    print("JIRA CONNECTION TROUBLESHOOTING")
    print("="*60)
    
    # Test URL format
    test_url = input("Enter your Jira URL (e.g., https://yourcompany.atlassian.net): ").strip()
    
    if not test_url.startswith('http'):
        print("❌ URL should start with https://")
        return False
    
    if '.atlassian.net' not in test_url and 'localhost' not in test_url:
        print("⚠️  Are you sure this is a correct Jira URL?")
    
    # Create session with proxy
    session = requests.Session()
    if proxy_config:
        session.proxies.update(proxy_config)
        print(f"Using proxy: {proxy_config}")
    
    # Test basic connectivity
    print(f"\n1. Testing basic connectivity to: {test_url}")
    try:
        response = session.get(f"{test_url}/status", timeout=15)
        print(f"✅ Basic connectivity OK (Status: {response.status_code})")
    except requests.exceptions.ProxyError as e:
        print(f"❌ Proxy error: {e}")
        print("Try these solutions:")
        print("- Check proxy settings")
        print("- Contact IT for correct proxy configuration")
        print("- Try connecting without proxy if possible")
        return False
    except Exception as e:
        print(f"❌ Basic connectivity failed: {e}")
        return False
    
    # Test authentication endpoint
    username = input("Enter your Jira username/email: ").strip()
    api_token = input("Enter your API token: ").strip()
    
    print(f"\n2. Testing authentication...")
    try:
        auth_response = session.get(
            f"{test_url}/rest/api/3/myself",
            auth=HTTPBasicAuth(username, api_token),
            headers={
                'Accept': 'application/json',
                'X-Atlassian-Token': 'no-check'
            },
            timeout=20
        )
        
        if auth_response.status_code == 200:
            user_info = auth_response.json()
            print(f"✅ Authentication successful!")
            print(f"   User: {user_info.get('displayName', 'Unknown')}")
            print(f"   Email: {user_info.get('emailAddress', 'Unknown')}")
            return test_url, username, api_token
        else:
            print(f"❌ Authentication failed (Status: {auth_response.status_code})")
            print(f"   Response: {auth_response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Authentication test failed: {e}")
        return False

def main():
    # Get proxy configuration first
    proxy_config = get_proxy_configuration()
    
    # Then run connectivity test
    conn_result = test_basic_connectivity(proxy_config)
    
    if not conn_result:
        print("\n❌ Connection test failed. Please check your URL and credentials.")
        return
    
    JIRA_URL, USERNAME, API_TOKEN = conn_result
    
    print(f"\n3. Proceeding with field retrieval...")
    
    # Create Jira API client
    jira = JiraAPI(JIRA_URL, USERNAME, API_TOKEN, proxy_config)
    
    # Test connection first
    print("Testing connection...")
    if not jira.test_connection():
        print("Failed to connect to Jira. Please check your credentials and URL.")
        return
    
    print("\n" + "="*50)
    print("Fetching all fields...")
    
    # Fetch all fields
    fields = jira.get_fields()
    
    if fields:
        print(f"Successfully fetched {len(fields)} fields:")
        print("\nField Summary:")
        print("-" * 80)
        print(f"{'ID':<20} {'Name':<30} {'Type':<15} {'Custom'}")
        print("-" * 80)
        
        for field in fields[:10]:  # Show first 10 fields
            field_id = field.get('id', 'N/A')
            field_name = field.get('name', 'N/A')[:28]
            field_type = field.get('schema', {}).get('type', 'N/A')
            is_custom = 'Yes' if field.get('custom', False) else 'No'
            
            print(f"{field_id:<20} {field_name:<30} {field_type:<15} {is_custom}")
        
        if len(fields) > 10:
            print(f"... and {len(fields) - 10} more fields")
        
        # Show custom fields separately
        custom_fields = jira.get_custom_fields()
        if custom_fields:
            print(f"\nFound {len(custom_fields)} custom fields:")
            for field in custom_fields[:5]:  # Show first 5 custom fields
                print(f"  - {field.get('name', 'N/A')} (ID: {field.get('id', 'N/A')})")
        
        # Save full response to file for inspection
        with open('jira_fields.json', 'w') as f:
            json.dump(fields, f, indent=2)
        print(f"\nFull field data saved to 'jira_fields.json'")
        
    else:
        print("Failed to fetch fields. Check the error messages above.")

if __name__ == "__main__":
    main()
